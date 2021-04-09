"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};


const expressionFactory = (function () {
    const AbstractExpressionPrototype = {
        simplify: function () { return this; },
        toString: function () { return this.value.toString(); },
        prefix: function () { return this.value.toString(); },
        postfix: function () { return this.value.toString(); },
        equals: function (expr) { return expr.constructor === this.constructor && this.equalsImpl(expr); }
    }
    return function (name, init, isCorrectArguments, evaluate, diff, equalsImpl) {
        function Expression(...args) {
            if (!this.isCorrectArguments(args)) {
                throw new ArgumentsError(this.constructor.name, ...args);
            }
            init(this, ...args);
        }
        Expression.prototype = Object.create(AbstractExpressionPrototype);
        Expression.prototype.constructor = Expression;
        Object.defineProperty(Expression, "name", {value: name});
        Expression.prototype.isCorrectArguments = isCorrectArguments;
        Expression.prototype.evaluate = evaluate;
        Expression.prototype.diff = diff;
        Expression.prototype.equalsImpl = equalsImpl;
        return Expression;
    }
})();


const operationFactory = (function () {
    const AbstractOperation = {
        name: "AbstractOperation",
        isCorrectArguments: function (...terms) {
            return (this.evaluateImpl.length === 0 || this.evaluateImpl.length === terms.length)
                && this.isCorrectArgumentsImpl(...terms);
        },
        evaluate: function (...args) { return this.evaluateImpl(...this.terms.map(expr => expr.evaluate(...args))); },
        diff: function (varName) { return this.diffImpl(varName, ...this.terms); },
        toString: function () {
            return String.prototype.concat(...this.terms.map(term => `${term.toString()} `), this.operator);
        },
        prefix: function () {
            return String.prototype.concat('(', this.operator, ...this.terms.map(term => ` ${term.prefix()}`), ')');
        },
        postfix: function () {
            return String.prototype.concat('(', ...this.terms.map(term => `${term.postfix()} `), this.operator, ')');
        },
        simplify: function () {
            let simples = this.terms.map(expr => expr.simplify());
            for (let simple of simples) {
                if (simple.constructor !== Const) {
                    return this.simplifyImpl(...simples);
                }
            }
            return new Const(this.evaluateImpl(...simples.map(term => term.value)));
        },
        equals: function (expr) { return expr.constructor === this.constructor && this.equalsImpl(expr); }
    }
    return function (name, operator, isCorrectTermsImpl, evaluateImpl, diffImpl, simplifyImpl, equalsImpl) {
        function OperationConstructor(...terms) {
            if (!this.isCorrectArguments(...terms)) {
                throw new ArgumentsError(this.constructor.name, ...terms);
            }
            Object.defineProperty(this, "terms", {value: terms});
        }
        OperationConstructor.prototype = Object.create(AbstractOperation);
        OperationConstructor.prototype.constructor = OperationConstructor;
        Object.defineProperty(OperationConstructor, "name", {value: name});
        OperationConstructor.prototype.isCorrectArgumentsImpl = isCorrectTermsImpl;
        OperationConstructor.prototype.evaluateImpl = evaluateImpl;
        OperationConstructor.prototype.diffImpl = diffImpl;
        OperationConstructor.prototype.simplifyImpl = simplifyImpl;
        OperationConstructor.prototype.equalsImpl = equalsImpl;
        OperationConstructor.prototype.operator = operator;
        OPERATIONS[operator] = OperationConstructor;
        return OperationConstructor;
    }
})();


const Const = expressionFactory(
    "Const",
    (obj, value) => { Object.defineProperty(obj, "value", {value: value}); },
    (value) => !isNaN(value),
    function () { return this.value; },
    () => ZERO,
    function (expr) { return expr.value === this.value; }
);

const ZERO = new Const(0);
const ONE = new Const(1);
const NEG_ONE = new Const(-1);
const TWO = new Const(2);


const Variable = expressionFactory(
    "Variable",
    (obj, value) => {
        Object.defineProperty(obj, "argPos", {value: ARGUMENT_POSITION[value]});
        Object.defineProperty(obj, "value", {value: value});
    },
    (name) => ARGUMENT_POSITION.hasOwnProperty(name),
    function (...args) { return args[this.argPos]; },
    function (varName) { return this.value === varName ? ONE : ZERO; },
    function (expr) { return expr.value === this.value; }
);


const Add = operationFactory(
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


const Subtract = operationFactory(
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


const Multiply = operationFactory(
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


const Divide = operationFactory(
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


const Negate = operationFactory(
    "Negate", "negate",
    () => true,
    (x) => -x,
    (varName, f) => new Negate(f.diff(varName)),
    (f) => new Multiply(NEG_ONE, f),
    nonCommutativeEquals,
);


const Hypot = operationFactory(
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


const HMean = operationFactory(
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


const Pow = operationFactory(
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


const Sign = operationFactory(
    "Sign", "sgn",
    () => true,
    Math.sign,
    undefined,
    function (f) { return new Sign(f); },
    nonCommutativeEquals
);


const ArithMean = operationFactory(
    "ArithMean", "arith-mean",
    (...terms) => terms.length > 0,
    (...a) => a.reduce((acc, cur) => acc + cur, 0) / a.length,
    (varName, ...terms) => new Multiply(new Const(1 / terms.length),
        terms.reduce((acc, cur) => new Add(acc, cur.diff(varName)), ZERO)
    ),
    function (...funcs) { return new ArithMean(...funcs); },
    commutativeEquals
);


const GeomMean = operationFactory(
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


const HarmMean = operationFactory(
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


function abstractParser(expression, isPrefix) {
    if (expression.length === 0) {
        throw new ParseError(0, '', "bracket expression, variable or constant");
    }
    const gen = abstractParser.tokenGenerator(expression);
    let res = abstractParser.parseRec(gen.next(), gen, isPrefix);
    let token = gen.next();
    abstractParser.checkToken(token, "end of expression", !token.done)
    return res;
}
abstractParser.tokenGenerator = function* (expression) {
    for (let match of expression.trim().matchAll(/\(|\)|[^()\s]+/g)) {
        yield {index: match.index, word: match[0]};
    }
    return {index: expression.trim().length, word: ''};
}
abstractParser.checkToken = (token, expected, condition) => {
    if (condition) { throw new ParseError(token.value.index, token.value.word, expected); }
}
abstractParser.parseRec = (token, gen, isPrefix) => {
    abstractParser.checkToken(token, "bracket expression, variable or constant", token.done);
    if (token.value.word === '(') {
        let args = [];
        let op;
        token = gen.next();
        abstractParser.checkToken(token, "operator", token.done);
        if (isPrefix) {
            if (OPERATIONS.hasOwnProperty(token.value.word)) {
                op = OPERATIONS[token.value.word];
                token = gen.next();
                while (!token.done && token.value.word !== ')') {
                    args.push(abstractParser.parseRec(token, gen, isPrefix));
                    token = gen.next();
                }
                abstractParser.checkToken(token, ")", token.done);
            } else {
                throw new ParseError(token.value.index, token.value.word, "operator");
            }
        } else {
            while (!token.done && token.value.word !== ')' && !OPERATIONS.hasOwnProperty(token.value.word)) {
                args.push(abstractParser.parseRec(token, gen, isPrefix));
                token = gen.next();
            }
            abstractParser.checkToken(token, "operator", token.done || token.value.word === ')');
            op = OPERATIONS[token.value.word];
            token = gen.next();
            abstractParser.checkToken(token, ")", token.done || token.value.word !== ')');
        }
        return new op(...args);
    } else if (ARGUMENT_POSITION.hasOwnProperty(token.value.word)) {
        return new Variable(token.value.word);
    } else {
        try {
            return new Const(+token.value.word);
        } catch (e) {
            throw new ParseError(token.value.index, token.value.word, "variable or constant");
        }
    }
}


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
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => {
            array.push(elem);
            array.push(elem);
        });
    } else {
        array.push(expr);
    }
    return array;
}


function nonCommutativeEquals(expr) {
    let ind = 0;
    return this.terms.reduce((acc, cur) => acc && cur.equals(expr.terms[ind++]), true);
}

function commutativeEquals(expr) {
    let res = false;
    for (let p of commutativeEquals.permutationGenerator(this.evaluateImpl.length)) {
        let ind = 0;
        res ||= this.terms.reduce((acc, cur) => acc && cur.equals(expr.terms[p[ind++]]), true);
    }
    return res;
}

commutativeEquals.permutationGenerator = function* (n) {
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
        commutativeEquals.permutationGenerator.swap(p, i, j);
        for (j = 1; i + j < n - j; j++) {
            commutativeEquals.permutationGenerator.swap(p, i + j, n - j);
        }
    }
}

commutativeEquals.permutationGenerator.swap = function (array, i, j) {
    let tmp = array[j];
    array[j] = array[i];
    array[i] = tmp;
}
