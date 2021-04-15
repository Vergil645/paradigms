"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = Object.freeze({"x": 0, "y": 1, "z": 2});


const expressionFactory = (function () {
    const abstractExpression = {
        simplify: function () { return this; },
        equals: function (expr) { return this.constructor === expr.constructor && expr.value === this.value; }
    }
    abstractExpression.toString = abstractExpression.prefix = abstractExpression.postfix = function () {
        return this.value.toString();
    }
    return Object.freeze({
        abstractExpression: Object.freeze(abstractExpression),
        create: function (name, init, isCorrectArguments, evaluate, diff) {
            function Expression(...items) {
                if (!isCorrectArguments(...items)) {
                    throw new ArgumentsError(this.constructor.name, ...items);
                }
                init(this, ...items);
            }
            Object.defineProperty(Expression, "name", {value: name});
            Expression.prototype = Object.create(abstractExpression, {
                constructor: {value: Expression},
                isCorrectArguments: {value: isCorrectArguments},
                evaluate: {value: evaluate},
                diff: {value: diff}
            });
            return Expression;
        }
    })
})();


const operationFactory = (function () {
    function createString(expr, printFunc) { return expr.terms.map((term) => term[printFunc]()).join(' '); }
    const abstractOperation = Object.create(expressionFactory.abstractExpression, {
        isCorrectArguments: {value: function (...terms) {
            return (this.evaluateImpl.length === 0 || this.evaluateImpl.length === terms.length)
                && this.isCorrectArgumentsImpl(...terms);
        }},
        evaluate: {value: function (...args) { return this.evaluateImpl(...this.terms.map(expr => expr.evaluate(...args))); }},
        diff: {value: function (varName) { return this.diffImpl(varName, ...this.terms); }},
        toString: {value: function () { return `${createString(this, "toString")} ${this.operator}`; }},
        prefix: {value: function () { return `(${this.operator} ${createString(this, "prefix")})`; }},
        postfix: {value: function () { return `(${createString(this, "postfix")} ${this.operator})`; }},
        simplify: {value: function () {
            let simples = this.terms.map(expr => expr.simplify());
            for (let simple of simples) {
                if (simple.constructor !== Const) {
                    return this.simplifyImpl(...simples);
                }
            }
            return new Const(this.evaluateImpl(...simples.map(term => term.value)));
        }},
        equals: {value: function (expr) {
            return expr.constructor === this.constructor && this.terms.length === expr.terms.length && this.equalsImpl(expr);
        }}
    });
    return Object.freeze({
        abstractOperation: abstractOperation,
        create: function(name, operator, isCorrectArgumentsImpl, evaluateImpl, diffImpl, simplifyImpl, equalsImpl) {
            function OperationConstructor(...terms) {
                if (!this.isCorrectArguments(...terms)) {
                    throw new ArgumentsError(this.constructor.name, ...terms);
                }
                Object.defineProperty(this, "terms", {value: terms});
            }
            Object.defineProperty(OperationConstructor, "name", {value: name});
            OperationConstructor.prototype = Object.create(abstractOperation, {
                constructor: {value: OperationConstructor},
                isCorrectArgumentsImpl: {value: isCorrectArgumentsImpl},
                evaluateImpl: {value: evaluateImpl},
                diffImpl: {value: diffImpl},
                simplifyImpl: {value: simplifyImpl},
                equalsImpl: {value: equalsImpl},
                operator: {value: operator},
            });
            OPERATIONS[operator] = OperationConstructor;
            return OperationConstructor;
        }
    })
})();

function nonCommutativeEquals(expr) {
    let ind = 0;
    return this.terms.reduce((acc, cur) => acc && cur.equals(expr.terms[ind++]), true);
}

const commutativeEquals = (function () {
    function* permutationGenerator(n) {
        let p = [];
        for (let i = 0; i < n; i++) { p.push(i); }
        while (true) {
            yield p;
            let i = n - 2;
            for (; i >= 0 && p[i] > p[i + 1]; i--) {}
            if (i < 0) {
                return;
            }
            let j = n - 1;
            for (; p[i] > p[j]; j--) {}
            swap(p, i, j);
            for (j = 1; i + j < n - j; j++) { swap(p, i + j, n - j); }
        }
    }
    function swap(array, i, j) {
        let tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
    }
    return function (expr) {
        let res = false;
        for (let p of permutationGenerator(this.terms.length)) {
            let ind = 0;
            res ||= this.terms.reduce((acc, cur) => acc && cur.equals(expr.terms[p[ind++]]), true);
        }
        return res;
    }
})()


const Const = expressionFactory.create(
    "Const",
    (obj, value) => { Object.defineProperty(obj, "value", {value: value}); },
    (value) => !isNaN(value),
    function () { return this.value; },
    () => ZERO
);

const ZERO = new Const(0);
const ONE = new Const(1);
const NEG_ONE = new Const(-1);
const TWO = new Const(2);


const Variable = expressionFactory.create(
    "Variable",
    (obj, value) => {
        Object.defineProperty(obj, "argPos", {value: ARGUMENT_POSITION[value]});
        Object.defineProperty(obj, "value", {value: value});
    },
    (name) => ARGUMENT_POSITION.hasOwnProperty(name),
    function (...args) { return args[this.argPos]; },
    function (varName) { return this.value === varName ? ONE : ZERO; }
);


const Add = operationFactory.create(
    "Add", "+",
    () => true,
    (x, y) => x + y,
    (varName, f, g) => new Add(f.diff(varName), g.diff(varName)),
    (f, g) => {
        if (f.equals(ZERO)) { return g; }
        else if (g.equals(ZERO)) { return f; }
        return new Add(f, g);
    },
    commutativeEquals,
);


const Subtract = operationFactory.create(
    "Subtract", "-",
    () => true,
    (x, y) => x - y,
    (varName, f, g) => new Subtract(f.diff(varName), g.diff(varName)),
    (f, g) => {
        if (f.equals(ZERO)) { return new Multiply(NEG_ONE, g); }
        else if (g.equals(ZERO)) { return f; }
        return new Subtract(f, g);
    },
    nonCommutativeEquals,
);


const Multiply = operationFactory.create(
    "Multiply", "*",
    () => true,
    (x, y) => x * y,
    (varName, f, g) => new Add(new Multiply(f, g.diff(varName)), new Multiply(f.diff(varName), g)),
    (f, g) => {
        if (f.equals(ZERO) || g.equals(ZERO)) { return ZERO; }
        else if (f.equals(ONE)) { return g; }
        else if (g.equals(ONE)) { return f; }
        return f.equals(g) ? new Pow(f, TWO) : new Multiply(f, g);
    },
    commutativeEquals,
);


const Divide = operationFactory.create(
    "Divide", "/",
    () => true,
    (x, y) => x / y,
    (varName, f, g) => new Divide(
        new Subtract(new Multiply(f.diff(varName), g), new Multiply(f, g.diff(varName))),
        new Multiply(g, g)
    ),
    (f, g) => {
        if (f.equals(ZERO)) { return ZERO; }
        else if (g.equals(ONE)) { return f; }
        else {
            let array0 = createArrayOfMultipliers(f);
            let array1 = createArrayOfMultipliers(g);
            let simplifiedFlag = false;
            for (let i = 0; i < array0.length; i++) {
                let findFlag = false;
                for (let j = 0; j < array1.length; j++) {
                    if (array1[j] !== null && array0[i].equals(array1[j])) {
                        if (findFlag) {
                            array0[i] = null;
                            array1[j] = null;
                            simplifiedFlag = true;
                            break;
                        } else {
                            findFlag = true;
                        }
                    }
                }
            }
            return !simplifiedFlag ? new Divide(f, g) : Divide.prototype.simplifyImpl(
                array0.reduce((acc, cur) => cur !== null ? new Multiply(acc, cur) : acc, ONE).simplify(),
                array1.reduce((acc, cur) => cur !== null ? new Multiply(acc, cur) : acc, ONE).simplify()
            );
        }
    },
    nonCommutativeEquals,
);


const Negate = operationFactory.create(
    "Negate", "negate",
    () => true,
    (x) => -x,
    (varName, f) => new Negate(f.diff(varName)),
    (f) => new Multiply(NEG_ONE, f),
    nonCommutativeEquals,
);


const Hypot = operationFactory.create(
    "Hypot", "hypot",
    () => true,
    (x, y) => x * x + y * y,
    (varName, f, g) => new Add(
        new Multiply(TWO, new Multiply(f, f.diff(varName))),
        new Multiply(TWO, new Multiply(g, g.diff(varName)))
    ),
    (f, g) => {
        if (f.equals(ZERO)) { return new Pow(g, TWO); }
        else if (g.equals(ZERO)) { return new Pow(f, TWO); }
        return new Hypot(f, g);
    },
    commutativeEquals,
);


const HMean = operationFactory.create(
    "HMean", "hmean",
    () => true,
    (x, y) => 2 / (1 / x + 1 / y),
    (varName, f, g) => new Divide(new Multiply(TWO, new Add(
            new Multiply(new Pow(g, TWO), f.diff(varName)),
            new Multiply(new Pow(f, TWO), g.diff(varName)))
        ),
        new Pow(new Add(f, g), TWO)
    ),
    (f, g) => f.equals(ZERO) || g.equals(ZERO) ? ZERO : new HMean(f, g),
    commutativeEquals,
);


const Pow = operationFactory.create(
    "Pow", "^",
    (f, g) => g.constructor === Const,
    (x, y) => x ** y,
    (varName, f, p) => new Multiply(new Const(p.value),
        new Multiply(new Pow(f, new Const(p.value - 1)), f.diff(varName))
    ),
    (f, g) => {
        if (f.equals(ZERO)) { return ZERO; }
        else if (g.equals(ZERO)) { return ONE; }
        else if (g.equals(ONE)) { return f; }
        return new Pow(f, g);
    },
    nonCommutativeEquals,
);


const Sign = operationFactory.create(
    "Sign", "sgn",
    () => true,
    Math.sign,
    () => { throw new Error("Sign has no diff() function"); },
    function (f) { return new Sign(f); },
    nonCommutativeEquals
);


const ArithMean = operationFactory.create(
    "ArithMean", "arith-mean",
    (...terms) => terms.length > 0,
    (...a) => a.reduce((acc, cur) => acc + cur, 0) / a.length,
    (varName, ...terms) => new Multiply(new Const(1 / terms.length),
        terms.reduce((acc, cur) => new Add(acc, cur.diff(varName)), ZERO)
    ),
    function (...funcs) { return new ArithMean(...funcs); },
    commutativeEquals
);


const GeomMean = operationFactory.create(
    "GeomMean", "geom-mean",
    (...terms) => terms.length > 0,
    (...a) => a.reduce((acc, cur) => Math.abs(acc * cur), 1) ** (1 / a.length),
    (varName, ...terms) => {
        let tmp = terms.reduce((acc, cur) => new Multiply(acc, cur), ONE);
        return new Multiply(new Const(1 / terms.length), new Divide(
                new Multiply(new Sign(tmp), tmp.diff(varName)),
                new Pow(new GeomMean(...terms), new Const(terms.length - 1))
            )
        )
    },
    function (...funcs) { return new GeomMean(...funcs); },
    commutativeEquals
);


const HarmMean = operationFactory.create(
    "HarmMean", "harm-mean",
    (...terms) => terms.length > 0,
    (...a) => a.length / a.reduce((acc, cur) => acc + 1 / cur, 0),
    (varName, ...terms) => new Multiply(new Const(1 / terms.length), new Multiply(
            new Pow(new HarmMean(...terms), TWO),
            terms.reduce((acc, cur) => new Add(acc, new Divide(cur.diff(varName), new Pow(cur, TWO))), ZERO)
        )
    ),
    function (...funcs) { return new HarmMean(...funcs); },
    commutativeEquals
);


function parse(expression) {
    const stack = [];
    for (const word of expression.trim().split(/\s+/)) {
        if (OPERATIONS.hasOwnProperty(word)) {
            const op = OPERATIONS[word];
            stack.push(new op(...stack.splice(-op.prototype.evaluateImpl.length)));
        } else if (word in ARGUMENT_POSITION) {
            stack.push(new Variable(word));
        } else if (word in CONSTANTS) {
            stack.push(new CONSTANTS[word]);
        } else {
            stack.push(new Const(+word));
        }
    }
    return stack.pop();
}

const abstractParser = (function () {
    function* tokenGenerator(expression) {
        for (let match of expression.trim().matchAll(/[()]|[^()\s]+/g)) {
            yield {index: match.index, word: match[0]};
        }
        return {index: expression.trim().length, word: ''};
    }
    function checkToken(token, expected, condition) {
        if (condition) { throw new ParseError(token.value.index, token.value.word, expected); }
    }
    function parseRec (token, gen, isPrefix) {
        checkToken(token, "bracket expression, variable or constant", token.done);
        if (token.value.word === '(') {
            let op, items = [];
            token = gen.next();
            if (isPrefix) {
                checkToken(token, "operator", token.done || !OPERATIONS.hasOwnProperty(token.value.word));
                op = OPERATIONS[token.value.word];
                token = gen.next();
            }
            while (!token.done && token.value.word !== ')' && (isPrefix || !OPERATIONS.hasOwnProperty(token.value.word))) {
                items.push(parseRec(token, gen, isPrefix));
                token = gen.next();
            }
            if (!isPrefix) {
                checkToken(token, "operator", token.done || token.value.word === ')');
                op = OPERATIONS[token.value.word];
                token = gen.next();
            }
            checkToken(token, ')', token.done || token.value.word !== ')')
            return new op(...items);
        } else if (ARGUMENT_POSITION.hasOwnProperty(token.value.word)) {
            return new Variable(token.value.word);
        } else {
            checkToken(token, "variable or constant", isNaN(token.value.word));
            return new Const(+token.value.word);
        }
    }
    return function (expression, isPrefix) {
        if (expression.length === 0) {
            throw new ParseError(0, '', "bracket expression, variable or constant");
        }
        const gen = tokenGenerator(expression);
        let res = parseRec(gen.next(), gen, isPrefix);
        let token = gen.next();
        checkToken(token, "end of expression", !token.done)
        return res;
    }
})();


function parsePrefix(expression) { return abstractParser(expression, true); }

function parsePostfix(expression) { return abstractParser(expression, false); }


function errorPrototypeFactory(Constructor) {
    Constructor.prototype = Object.create(Error.prototype);
    Constructor.prototype.constructor = Constructor;
}

function ArgumentsError(funcName, ...args) { this.message = `Invalid arguments of function ${funcName}: ${args}`; }
errorPrototypeFactory(ArgumentsError);

function ParseError(begin, word, expected) {
    this.message = `Invalid symbol on positions: ${begin + 1}-${begin + Math.max(1, word.length)}\n`
        + `Expected: ${expected}\nFound: '${word}'`;
}
errorPrototypeFactory(ParseError);


function createArrayOfMultipliers(expr) {
    let array = [];
    if (expr.constructor === Multiply) {
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => { array.push(elem); });
        createArrayOfMultipliers(expr.terms[1]).forEach((elem) => { array.push(elem); });
    } else if (expr.constructor === Pow && expr.terms[1].value === 2) {
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => { array.push(elem); array.push(elem); });
    } else {
        array.push(expr);
    }
    return array;
}

// println(parsePrefix("(+ 100x)"))
// println(parsePostfix("(x y (2 3 +))"))
// println(parsePrefix("10"))
// println(parsePrefix("jdfhgkdhfj"))
// println(parsePrefix("NaN"))
// println(parsePrefix("Infinity"))
