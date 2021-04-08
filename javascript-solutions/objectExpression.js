"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};


const AbstractExpressionPrototype = {
    equals: function (expr) {
        return expr.constructor === this.constructor && this.equalsImpl(expr);
    }
}

function expressionFactory(Constructor, isCorrectArguments, evaluate, diff, simplify, toString, prefix, equalsImpl) {
    Constructor.prototype = Object.create(AbstractExpressionPrototype);
    Constructor.prototype.constructor = Constructor;
    Constructor.prototype.isCorrectArguments = isCorrectArguments;
    Constructor.prototype.evaluate = evaluate;
    Constructor.prototype.diff = diff;
    Constructor.prototype.simplify = simplify;
    Constructor.prototype.toString = toString;
    Constructor.prototype.prefix = prefix;
    Constructor.prototype.equalsImpl = equalsImpl;
    return Constructor;
}


const Const = expressionFactory(
    function (value) {
        if (!this.isCorrectArguments(value)) {
            throw new ArgumentsError(Const, value);
        }
        Object.defineProperty(this, "value", {value: value});
    },
    (value) => !isNaN(value),
    function () {
        return this.value;
    },
    () => ZERO,
    function () {
        return this;
    },
    function () {
        return this.value.toString();
    },
    function () {
        return this.value.toString();
    },
    function (expr) {
        return expr.value === this.value;
    }
);

const ZERO = new Const(0);
const ONE = new Const(1);
const NEG_ONE = new Const(-1);
const TWO = new Const(2);


const Variable = expressionFactory(
    function (name) {
        if (!this.isCorrectArguments(name)) {
            throw new ArgumentsError(Variable, name);
        }
        Object.defineProperty(this, "argPos", {value: ARGUMENT_POSITION[name]});
        Object.defineProperty(this, "name", {value: name});
    },
    (name) => typeof name === "string",
    function (...args) {
        return args[this.argPos];
    },
    function (varName) {
        return this.name === varName ? ONE : ZERO;
    },
    function () {
        return this;
    },
    function () {
        return this.name;
    },
    function () {
        return this.name;
    },
    function (expr) {
        return expr.name === this.name;
    }
);


const AbstractOperation = expressionFactory(
    function (...terms) {
        if (!this.isCorrectArguments(...terms)) {
            throw new ArgumentsError(this.constructor, ...terms);
        }
        Object.defineProperty(this, "terms", {value: terms});
    },
    function (...terms) {
        return (this.evaluateImpl.length === 0 || this.evaluateImpl.length === terms.length)
            && this.isCorrectArgumentsImpl(...terms);
    },
    function (...args) {
        return this.evaluateImpl(...this.terms.map(expr => expr.evaluate(...args)));
    },
    function (varName) {
        return this.diffImpl(...this.terms, ...this.terms.map(term => term.diff(varName)));
    },
    function () {
        let simples = this.terms.map(expr => expr.simplify());
        for (let simple of simples) {
            if (simple.constructor !== Const) {
                return this.simplifyImpl(...simples);
            }
        }
        return new Const(this.evaluateImpl(...simples.map(term => term.value)));
    },
    function () {
        return String.prototype.concat(...this.terms.map(term => `${term.toString()} `), this.operator);
    },
    function () {
        return String.prototype.concat('(', this.operator, ...this.terms.map(term => ` ${term.prefix()}`), ')');
    }
);


function operationFactory(isCorrectTermsImpl, evaluateImpl, diffImpl, simplifyImpl, equalsImpl, ...operators) {
    function OperationConstructor(...terms) {
        AbstractOperation.call(this, ...terms);
    }

    OperationConstructor.prototype = Object.create(AbstractOperation.prototype);
    OperationConstructor.prototype.constructor = OperationConstructor;
    OperationConstructor.prototype.isCorrectArgumentsImpl = isCorrectTermsImpl;
    OperationConstructor.prototype.evaluateImpl = evaluateImpl;
    OperationConstructor.prototype.diffImpl = diffImpl;
    OperationConstructor.prototype.simplifyImpl = simplifyImpl;
    OperationConstructor.prototype.equalsImpl = equalsImpl;
    OperationConstructor.prototype.operator = operators[0];
    for (let operator of operators) {
        OPERATIONS[operator] = OperationConstructor;
    }
    return OperationConstructor;
}


const Add = operationFactory(
    () => true,
    (x, y) => x + y,
    (f, g, df, dg) => new Add(df, dg),
    (f, g) => {
        if (f.equals(ZERO)) {
            return g;
        } else if (g.equals(ZERO)) {
            return f;
        } else {
            return new Add(f, g);
        }
    },
    commutativeEquals,
    "+"
);


const Subtract = operationFactory(
    () => true,
    (x, y) => x - y,
    (f, g, df, dg) => new Subtract(df, dg),
    (f, g) => {
        if (f.equals(ZERO)) {
            return new Multiply(NEG_ONE, g);
        } else if (g.equals(ZERO)) {
            return f;
        } else {
            return new Subtract(f, g);
        }
    },
    nonCommutativeEquals,
    "-"
);


const Multiply = operationFactory(
    () => true,
    (x, y) => x * y,
    (f, g, df, dg) => {
        return new Add(new Multiply(f, dg), new Multiply(df, g));
    },
    (f, g) => {
        if (f.equals(ZERO) || g.equals(ZERO)) {
            return ZERO;
        } else if (f.equals(ONE)) {
            return g;
        } else if (g.equals(ONE)) {
            return f;
        }
        return f.equals(g) ? new Pow(f, TWO) : new Multiply(f, g);
    },
    commutativeEquals,
    "*"
);


const Divide = operationFactory(
    () => true,
    (x, y) => x / y,
    (f, g, df, dg) => new Divide(new Subtract(new Multiply(df, g), new Multiply(f, dg)), new Multiply(g, g)),
    (f, g) => {
        if (f.equals(ZERO)) {
            return ZERO;
        } else if (g.equals(ONE)) {
            return f;
        } else {
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
    "/"
);


const Negate = operationFactory(
    () => true,
    (x) => -x,
    (f, df) => new Negate(df),
    (f) => new Multiply(NEG_ONE, f),
    nonCommutativeEquals,
    "negate"
);


const Hypot = operationFactory(
    () => true,
    (x, y) => x * x + y * y,
    (f, g, df, dg) => new Add(Pow.prototype.diffImpl(f, TWO, df), Pow.prototype.diffImpl(g, TWO, dg)),
    (f, g) => {
        if (f.equals(ZERO)) {
            return new Pow(g, TWO);
        } else if (g.equals(ZERO)) {
            return new Pow(f, TWO);
        } else {
            return new Hypot(f, g);
        }
    },
    commutativeEquals,
    "hypot"
);


const HMean = operationFactory(
    () => true,
    (x, y) => 2 / (1 / x + 1 / y),
    (f, g, df, dg) => {
        return new Divide(
            new Multiply(TWO, new Add(new Multiply(new Pow(g, TWO), df), new Multiply(new Pow(f, TWO), dg))),
            new Pow(new Add(f, g), TWO)
        );
    },
    (f, g) => {
        return f.equals(ZERO) || g.equals(ZERO) ? ZERO : new HMean(f, g);
    },
    commutativeEquals,
    "hmean"
);


const Pow = operationFactory(
    (f, g) => g.constructor === Const,
    (x, y) => x ** y,
    (f, g, df) => new Multiply(new Const(g.value), new Multiply(new Pow(f, new Const(g.value - 1)), df)),
    (f, g) => {
        if (f.equals(ZERO)) {
            return ZERO;
        } else if (g.equals(ZERO)) {
            return ONE;
        } else if (g.equals(ONE)) {
            return f;
        } else {
            return new Pow(f, g);
        }
    },
    nonCommutativeEquals,
    "^"
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


function parsePrefix(expression) {
    const gen = expression.trim().matchAll(/\(|\)|[^()\s]+/g);

    function parseRec(token) {
        if (token.done) {
            throw new ParseError(token, "bracket expression, variable or constant");
        }
        if (token.value[0] === '(') {
            token = gen.next();
            if (OPERATIONS.hasOwnProperty(token.value[0])) {
                let args = [];
                let op = OPERATIONS[token.value[0]];
                token = gen.next();
                while (!token.done && token.value[0] !== ')') {
                    args.push(parseRec(token));
                    token = gen.next();
                }
                if (token.done) {
                    throw new ParseError(token, ")");
                }
                return new op(...args);
            } else {
                throw new ParseError(token, "operator");
            }
        } else if (ARGUMENT_POSITION.hasOwnProperty(token.value[0])) {
            return new Variable(token.value[0]);
        } else {
            try {
                return new Const(+token.value[0]);
            } catch (e) {
                throw new ParseError(token, "variable or constant");
            }
        }
    }

    let res = parseRec(gen.next());
    let token = gen.next();
    if (!token.done) {
        throw new ParseError(token, "end of expression");
    }
    return res;
}


class ArgumentsError extends Error {
    constructor(func, ...args) {
        super(`Invalid arguments of function ${func.name}: ${args}`);
    }
}

class ParseError extends Error {
    constructor(token, expected) {
        super(`Invalid symbol on positions: ${token.value.index + 1}-${token.value.index + token.value[0].length}\n`
            + `Expected: ${expected}\nFound: '${token.value[0]}'`
        );
    }
}


function createArrayOfMultipliers(expr) {
    let array = [];
    if (expr.constructor === Multiply) {
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => {
            array.push(elem)
        });
        createArrayOfMultipliers(expr.terms[1]).forEach((elem) => {
            array.push(elem)
        });
    } else if (expr.constructor === Pow && Number.isInteger(expr.terms[1].value) && expr.terms[1].value > 0) {
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => {
            for (let i = 0; i < expr.terms[1].value; i++) {
                array.push(elem);
            }
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
    for (let i = 0; i < n; i++) {
        p.push(i);
    }
    while (true) {
        yield p;
        let i = n - 2;
        for (; i >= 0 && p[i] > p[i + 1]; i--) {
        }
        if (i < 0) {
            return;
        }
        let j = n - 1;
        for (; p[i] > p[j]; j--) {
        }
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


// println(parsePrefix("(/ (* (* y y) (^ x 5)) (* (^ y 3) (^ x 8)))").simplify().prefix())
// println(parsePrefix("(+ x 2))"))
// println(Object.getPrototypeOf(ParseError.prototype) === Error.prototype);
// println(parsePrefix("(^ x (+ y z) x)"))
// println(parsePrefix("(^ 3 (+ x y))").diff('x').prefix())
// println(0 ** 2)
// println(new Log(new Subtract(new Variable('y'), new Variable('z'))).evaluate(2.0, 2.0, 2.0))
// println(new Hypot(new Variable('x'), new Subtract(new Variable('y'), new Variable('z'))).diff('x').prefix())
// println(new Hypot(new Variable('x'), new Subtract(new Variable('y'), new Variable('z'))).diff('x').evaluate(2.0, 2.0, 2.0))
// println(parsePrefix("(log x)").diff('x').prefix())
// println(parsePrefix("(^ x 2)").diff('x').prefix())
// println(parsePrefix("(/ (* (* x y) (* x x)) (* (* y z) (* x x)))").simplify().prefix())

// const T = OperationFactory(
//     (x, y, z) => x + y + z,
//     undefined,
//     undefined,
//     commutativeEquals,
//     ""
// )
// println(new T(new Add(ZERO, ONE), ONE, TWO).equals(new T(TWO, ONE, new Add(ONE, ZERO))))

// println(new Add(Const.ZERO, Const.ZERO).constructor === Add)
// println(parse('x y z * /').diff('x').simplify().prefix())
// println(parse('x y z - hypot').diff('z').simplify());
// (/ (* (* 2 y) y) (* (+ x y) (+ x y)))
// (/ 2 (* (+ (/ x y) 1) (+ (/ x y) 1)))
// (* 2 (* (/ y (+ x y)) (/ y (+ x y))))
// println(parse('x y hmean').diff('x').simplify().prefix());
// println(parse('5 z /').diff('z').expressions[0].simplify());
// println(parse('5 z /').diff('z').expressions[1].simplify());
// println(parse('5 z /').diff('z').simplify());
// println(parsePrefix("(/ (negate x) 2)"));
