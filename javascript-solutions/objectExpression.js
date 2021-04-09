"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};


const expressionFactory = (function () {
    const AbstractExpressionPrototype = {
        equals: function (expr) {
            return expr.constructor === this.constructor && this.equalsImpl(expr);
        }
    }
    return function (name, Constructor, isCorrectArguments, evaluate, diff, simplify, toString, prefix, equalsImpl) {
        Constructor.prototype = Object.create(AbstractExpressionPrototype);
        Constructor.prototype.constructor = Constructor;
        Object.defineProperty(Constructor, "name", {value: name});
        Constructor.prototype.isCorrectArguments = isCorrectArguments;
        Constructor.prototype.evaluate = evaluate;
        Constructor.prototype.diff = diff;
        Constructor.prototype.simplify = simplify;
        Constructor.prototype.toString = toString;
        Constructor.prototype.prefix = prefix;
        Constructor.prototype.equalsImpl = equalsImpl;
        return Constructor;
    }
})();


const operationFactory = (function () {
    const AbstractOperation = expressionFactory(
        "AbstractOperation",
        function (...terms) {
            if (!this.isCorrectArguments(...terms)) {
                throw new ArgumentsError(this.constructor.name, ...terms);
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
    return function (name, isCorrectTermsImpl, evaluateImpl, diffImpl, simplifyImpl, equalsImpl, ...operators) {
        function OperationConstructor(...terms) {
            AbstractOperation.call(this, ...terms);
        }

        OperationConstructor.prototype = Object.create(AbstractOperation.prototype);
        OperationConstructor.prototype.constructor = OperationConstructor;
        Object.defineProperty(OperationConstructor, "name", {value: name});
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
})();


const Const = expressionFactory(
    "Const",
    function (value) {
        if (!this.isCorrectArguments(value)) {
            throw new ArgumentsError(this.constructor.name, value);
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
    "Variable",
    function (name) {
        if (!this.isCorrectArguments(name)) {
            throw new ArgumentsError(this.constructor.name, name);
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


const Add = operationFactory(
    "Add",
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
    "Subtract",
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
    "Multiply",
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
    "Divide",
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
    "Negate",
    () => true,
    (x) => -x,
    (f, df) => new Negate(df),
    (f) => new Multiply(NEG_ONE, f),
    nonCommutativeEquals,
    "negate"
);


const Hypot = operationFactory(
    "Hypot",
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
    "HMean",
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
    "Pow",
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
    if (expression.length === 0) {
        throw new ParseError(0, '', "bracket expression, variable or constant");
    }
    const gen = expression.trim().matchAll(/\(|\)|[^()\s]+/g);

    function parseRec(token) {
        if (token.done) {
            throw new ParseError(expression.trim.length, '', "bracket expression, variable or constant");
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
                    throw new ParseError(expression.trim().length, '', ')');
                }
                return new op(...args);
            } else {
                throw new ParseError(token.value.index, token.value[0], "operator");
            }
        } else if (ARGUMENT_POSITION.hasOwnProperty(token.value[0])) {
            return new Variable(token.value[0]);
        } else {
            try {
                return new Const(+token.value[0]);
            } catch (e) {
                throw new ParseError(token.value.index, token.value[0], "variable or constant");
            }
        }
    }

    let res = parseRec(gen.next());
    let token = gen.next();
    if (!token.done) {
        throw new ParseError(token.value.index, token.value[0], "end of expression");
    }
    return res;
}


function errorPrototypeFactory(Constructor) {
    Constructor.prototype = Object.create(Error.prototype);
    Constructor.prototype.constructor = Constructor;
}

function ArgumentsError(funcName, ...args) {
    this.message = `Invalid arguments of function ${funcName}: ${args}`;
}
errorPrototypeFactory(ArgumentsError);

function ParseError(begin, word, expected) {
    this.message = `Invalid symbol on positions: ${begin + 1}-${begin + Math.max(1, word.length)}\n`
        + `Expected: ${expected}\nFound: '${word}'`;
}
errorPrototypeFactory(ParseError);


function createArrayOfMultipliers(expr) {
    let array = [];
    if (expr.constructor === Multiply) {
        createArrayOfMultipliers(expr.terms[0]).forEach((elem) => {
            array.push(elem)
        });
        createArrayOfMultipliers(expr.terms[1]).forEach((elem) => {
            array.push(elem)
        });
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
