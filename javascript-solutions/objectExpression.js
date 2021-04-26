"use strict";

const OPERATIONS = {};
const ARGUMENT_POSITION = Object.freeze({"x": 0, "y": 1, "z": 2});


const expressionFactory = (function () {
    const abstractExpression = {};
    abstractExpression.toString = abstractExpression.prefix = abstractExpression.postfix = function () {
        return this.value.toString();
    };
    return Object.freeze({
        abstractExpression: Object.freeze(abstractExpression),
        create: function (name, init, evaluate, diff) {
            function Expression(...items) {
                init(this, ...items);
            }
            Object.defineProperty(Expression, "name", {value: name});
            Expression.prototype = Object.create(abstractExpression, {
                constructor: {value: Expression},
                evaluate: {value: evaluate},
                diff: {value: diff}
            });
            return Expression;
        }
    })
})();


const operationFactory = (function () {
    function createString(expr, printFunc) {
        return expr.terms.map((term) => term[printFunc]()).join(' ');
    }
    const abstractOperation = Object.create(expressionFactory.abstractExpression, {
        evaluate: {value: function (...args) {
            return this.evaluateImpl(...this.terms.map(expr => expr.evaluate(...args)));
        }},
        diff: {value: function (varName) {
            return this.diffImpl(this.terms, this.terms.map((term) => term.diff(varName)));
        }},
        toString: {value: function () { return `${createString(this, "toString")} ${this.operator}`; }},
        prefix: {value: function () { return `(${this.operator} ${createString(this, "prefix")})`; }},
        postfix: {value: function () { return `(${createString(this, "postfix")} ${this.operator})`; }},
    });
    return Object.freeze({
        abstractOperation: abstractOperation,
        create: function(name, operator, evaluateImpl, diffImpl) {
            function Operation(...terms) {
                if (terms.length === 0 || (evaluateImpl.length !== 0 && evaluateImpl.length !== terms.length)) {
                    throw new ArgumentsError(name, ...terms);
                }
                Object.defineProperty(this, "terms", {value: terms});
            }
            Object.defineProperty(Operation, "name", {value: name});
            Operation.prototype = Object.create(abstractOperation, {
                constructor: {value: Operation},
                evaluateImpl: {value: evaluateImpl},
                diffImpl: {value: diffImpl},
                operator: {value: operator},
            });
            OPERATIONS[operator] = Operation;
            return Operation;
        }
    })
})();


const Const = expressionFactory.create(
    "Const",
    (obj, value) => { Object.defineProperty(obj, "value", {value: value}); },
    function () { return this.value; },
    () => ZERO
);

const ZERO = new Const(0);
const ONE = new Const(1);
const TWO = new Const(2);


const Variable = expressionFactory.create(
    "Variable",
    (obj, value) => {
        Object.defineProperty(obj, "argPos", {value: ARGUMENT_POSITION[value]});
        Object.defineProperty(obj, "value", {value: value});
    },
    function (...args) { return args[this.argPos]; },
    function (varName) { return this.value === varName ? ONE : ZERO; }
);


const Add = operationFactory.create(
    "Add", "+",
    (x, y) => x + y,
    (_, [df, dg]) => new Add(df, dg)
);


const Subtract = operationFactory.create(
    "Subtract", "-",
    (x, y) => x - y,
    (_, [df, dg]) => new Subtract(df, dg)
);


const Multiply = operationFactory.create(
    "Multiply", "*",
    (x, y) => x * y,
    ([f, g], [df, dg]) => new Add(new Multiply(f, dg), new Multiply(df, g))
);


const Divide = operationFactory.create(
    "Divide", "/",
    (x, y) => x / y,
    ([f, g], [df, dg]) => new Divide(
        new Subtract(new Multiply(df, g), new Multiply(f, dg)),
        new Multiply(g, g)
    )
);


const Negate = operationFactory.create(
    "Negate", "negate",
    (x) => -x,
    (_, [df]) => new Negate(df)
);


const Hypot = operationFactory.create(
    "Hypot", "hypot",
    (x, y) => x * x + y * y,
    ([f, g], [df, dg]) => new Add(
        new Multiply(TWO, new Multiply(f, df)),
        new Multiply(TWO, new Multiply(g, dg))
    )
);


const HMean = operationFactory.create(
    "HMean", "hmean",
    (x, y) => 2 / (1 / x + 1 / y),
    ([f, g], [df, dg]) => new Divide(
        new Multiply(TWO, new Add(
            new Multiply(new Pow(g, TWO), df),
            new Multiply(new Pow(f, TWO), dg))
        ),
        new Pow(new Add(f, g), TWO)
    )
);


const Pow = operationFactory.create(
    "Pow", "^",
    (x, y) => x ** y,
    ([f, g], [df, dg]) => new Multiply(
        new Pow(f, new Subtract(g, ONE)),
        new Add(new Multiply(df, g), new Multiply(new Multiply(f, new Log(f)), dg))
    )
);


const Log = operationFactory.create(
    "Log", "log",
    Math.log,
    ([f], [df]) => new Divide(df, f)
);


const ArithMean = operationFactory.create(
    "ArithMean", "arith-mean",
    (...a) => a.reduce((acc, cur) => acc + cur, 0) / a.length,
    (terms, termsDiff) => new Multiply(new Const(1 / terms.length),
        termsDiff.reduce((acc, cur) => new Add(acc, cur), ZERO)
    )
);


const GeomMean = operationFactory.create(
    "GeomMean", "geom-mean",
    (...a) => a.reduce((acc, cur) => Math.abs(acc * cur), 1) ** (1 / a.length),
    (terms, termsDiff) => {
        return new Multiply(new Const(1 / terms.length), new Multiply(
            new Divide(
                terms.reduce((acc, cur) => new Multiply(acc, cur), ONE),
                new Pow(new GeomMean(...terms), new Const(2 * terms.length - 1))
            ),
            terms.reduce((mulDiff, _, i) => new Add(
                mulDiff,
                terms.reduce((acc, cur, j) => new Multiply(acc, i === j ? termsDiff[i] : cur), ONE)), ZERO)
        ))
    }
);


const HarmMean = operationFactory.create(
    "HarmMean", "harm-mean",
    (...a) => a.length / a.reduce((acc, cur) => acc + 1 / cur, 0),
    (terms, termsDiff) => {
        return new Multiply(
            new Const(1 / terms.length),
            new Multiply(
                new Pow(new HarmMean(...terms), TWO),
                terms.reduce((acc, cur, i) => new Add(acc, new Divide(termsDiff[i], new Pow(cur, TWO))), ZERO)
            )
        );
    }
);


function parse(expression) {
    const stack = [];
    for (const word of expression.trim().split(/\s+/)) {
        if (OPERATIONS.hasOwnProperty(word)) {
            const op = OPERATIONS[word];
            stack.push(new op(...stack.splice(-op.prototype.evaluateImpl.length)));
        } else if (word in ARGUMENT_POSITION) {
            stack.push(new Variable(word));
        } else {
            stack.push(new Const(+word));
        }
    }
    return stack.pop();
}

const expressionParser = (function () {
    function* tokenGenerator(expression) {
        let exprTrim = expression.trim();
        for (let match of exprTrim.matchAll(/\(|\)|[^()\s]+/g)) {
            yield {index: match.index, word: match[0]};
        }
        return {index: exprTrim.length, word: ''};
    }
    function checkToken(token, expected, condition) {
        if (condition) { throw new ParseError(token.value.index, token.value.word, expected); }
    }
    function parseRec (token, gen, isPrefix) {
        checkToken(token, "bracket expression, variable or constant", token.done);
        if (token.value.word === '(') {
            let op, items = [];
            let beginToken = token;
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
            checkToken(token, ')', token.done || token.value.word !== ')');
            try {
                return new op(...items);
            } catch (e) {
                throw new ParseFunctionArgumentsError(`On positions ${beginToken.value.index + 1}-${token.value.index + 1}: ${e.message}`);
            }
        } else if (ARGUMENT_POSITION.hasOwnProperty(token.value.word)) {
            return new Variable(token.value.word);
        } else {
            checkToken(token, "variable or constant", !isFinite(token.value.word));
            return new Const(parseInt(token.value.word));
        }
    }
    return (isPrefix) => (expression) => {
        if (expression.length === 0) {
            throw new ParseError(0, '', "bracket expression, variable or constant");
        }
        const gen = tokenGenerator(expression);
        let res = parseRec(gen.next(), gen, isPrefix);
        let token = gen.next();
        checkToken(token, "end of expression", !token.done);
        return res;
    }
})();


const parsePrefix = expressionParser(true);
const parsePostfix = expressionParser(false);


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

function ParseFunctionArgumentsError(message) { this.message = message; }
errorPrototypeFactory(ParseFunctionArgumentsError);
