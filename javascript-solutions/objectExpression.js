"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};

function ExpressionFactory(Expression, evaluate, diff, simplify, toString, prefix, equals) {
    Expression.prototype = {
        constructor: Expression,
        evaluate: evaluate,
        diff: diff,
        simplify: simplify,
        toString: toString,
        prefix: prefix,
        equals: equals
    };
    return Expression;
}

function OperationFactory(evaluateImpl, diffImpl, simplifyImpl, equals, ...operators) {
    function Operation(...expressions) {
        AbstractOperation.call(this, ...expressions);
    }
    Operation.prototype = Object.create(AbstractOperation.prototype);
    Operation.prototype.constructor = Operation;
    Operation.prototype.evaluateImpl = evaluateImpl;
    Operation.prototype.diffImpl = diffImpl;
    Operation.prototype.simplifyImpl = simplifyImpl;
    Operation.prototype.equals = equals;
    Operation.prototype.operator = operators[0];
    for (let operator of operators)
        OPERATIONS[operator] = Operation;
    return Operation;
}

function parse(expression) {
    const stack = [];
    for (const token of expression.trim().split(/\s+/)) {
        if (token in OPERATIONS) {
            const op = OPERATIONS[token];
            stack.push(new op(...stack.splice(-op.prototype.evaluateImpl.length)));
        } else if (token in ARGUMENT_POSITION) {
            stack.push(new Variable(token));
        } else if (token in CONSTANTS) {
            stack.push(new CONSTANTS[token]);
        } else {
            stack.push(new Const(+token));
        }
    }
    return stack.pop();
}


const Const = ExpressionFactory(
    function (value) { this.value = value; },
    function () { return this.value; },
    () => Const.ZERO,
    function () { return new Const(this.value); },
    function () { return this.value.toString(); },
    function () { return this.value.toString(); },
    function (expression) { return Const.equals(expression, this.value); }
);
Const.ZERO = new Const(0);
Const.ONE = new Const(1);
Const.TWO = new Const(2);
Const.equals = (expression, value) => {
    return expression.constructor === Const && (value === undefined || expression.evaluate() === value);
};


const Variable = ExpressionFactory(
    function(name) {
        this.arg_pos = ARGUMENT_POSITION[name];
        this.name = name;
    },
    function (...args) { return args[this.arg_pos]; },
    function (var_name) { return this.name === var_name ? Const.ONE : Const.ZERO; },
    function () { return new Variable(this.name); },
    function () { return this.name; },
    function () { return this.name; },
    function (expression) { return expression.constructor === Variable && expression.name === this.name; }
);


const AbstractOperation = ExpressionFactory(
    function (...expressions) { this.expressions = expressions; },
    function (...args) {
        return this.evaluateImpl(...this.expressions.map(expr => expr.evaluate(...args)));
    },
    function (var_name) {
        return this.diffImpl(...this.expressions, ...this.expressions.map(expr => expr.diff(var_name)));
    },
    function () {
        let simples = this.expressions.map(expr => expr.simplify());
        for (let simple of simples) {
            if (!Const.equals(simple))
                return this.simplifyImpl(...simples);
        }
        return new Const(this.evaluateImpl(...simples.map(expr => expr.evaluate())));
    },
    function () {
        return String.prototype.concat(...this.expressions.map(expr => `${expr.toString()} `), this.operator);
    },
    function () {
        return String.prototype.concat('(', this.operator, ...this.expressions.map(expr => ` ${expr.prefix()}`), ')');
    },
    undefined
);


const Add = OperationFactory(
    (x, y) => x + y,
    (f, g, f_diff, g_diff) => new Add(f_diff, g_diff),
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0)) return simple_1;
        if (Const.equals(simple_1, 0)) return simple_0;
        return new Add(simple_0, simple_1);
    },
    function (expression) {
        return expression.constructor === Add
            && (expression.expressions === this.expressions || expression.expressions.reverse() === this.expressions);
    },
    "+"
);


const Subtract = OperationFactory(
    (x, y) => x - y,
    (f, g, f_diff, g_diff) => new Subtract(f_diff, g_diff),
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0)) return new Negate(simple_1);
        if (Const.equals(simple_1, 0)) return simple_0;
        return new Subtract(simple_0, simple_1);
    },
    function (expression) {
        return expression.constructor === Subtract && expression.expressions === this.expressions;
    },
    "-"
);


const Multiply = OperationFactory(
    (x, y) => x * y,
    (f, g, f_diff, g_diff) => {
        return new Add(new Multiply(f, g_diff), new Multiply(f_diff, g));
    },
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0) || Const.equals(simple_1, 0)) return new Const(0);
        if (Const.equals(simple_0, 1)) return simple_1;
        if (Const.equals(simple_1, 1)) return simple_0;
        if (Const.equals(simple_0, -1)) return new Negate(simple_1);
        if (Const.equals(simple_1, -1)) return new Negate(simple_0);
        return new Multiply(simple_0, simple_1);
    },
    function (expression) {
        return expression.constructor === Multiply
            && (expression.expressions === this.expressions || expression.expressions.reverse() === this.expressions);
    },
    "*"
);


const Divide = OperationFactory(
    (x, y) => x / y,
    (f, g, f_diff, g_diff) => {
        return new Divide(
            new Subtract(new Multiply(f_diff, g), new Multiply(f, g_diff)),
            new Multiply(g, g)
        );
    },
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0)) return new Const(0);
        if (Const.equals(simple_1, 1)) return simple_0;
        if (Const.equals(simple_1, -1)) return new Negate(simple_0);
        function createArray(expression, array) {
            if (expression.constructor === Multiply) {
                createArray(expression.expressions[0], array);
                createArray(expression.expressions[1], array);
            } else {
                array.push(expression);
            }
        }
        let array0 = [];
        createArray(simple_0, array0);
        let array1 = [];
        createArray(simple_1, array1);
        for (let i = 0; i < array0.length; i++) {
            let ind = array1.findIndex((expr) => expr != null && expr.equals(array0[i]));
            if (ind !== -1) {
                array0[i] = null;
                array1[ind] = null;
            }
        }
        simple_0 = Const.ONE;
        for (let expression of array0) {
            if (expression === null) {
                continue;
            }
            simple_0 = new Multiply(simple_0, expression);
        }
        simple_1 = Const.ONE;
        for (let expression of array1) {
            if (expression === null) {
                continue;
            }
            simple_1 = new Multiply(simple_1, expression);
        }
        return new Divide(simple_0.simplify(), simple_1.simplify());
    },
    function (expression) {
        return expression.constructor === Subtract && expression.expressions === this.expressions;
    },
    "/"
);


const Negate = OperationFactory(
    (x) => -x,
    (f, f_diff) => new Negate(f_diff),
    (simple_0) => new Negate(simple_0),
    function (expression) {
        return expression.constructor === Negate && expression.expressions === this.expressions;
    },
    "negate"
);


const Hypot = OperationFactory(
    (x, y) => x * x + y * y,
    (f, g, f_diff, g_diff) => {
        return new Add(
            Multiply.prototype.diffImpl(f, f, f_diff, f_diff),
            Multiply.prototype.diffImpl(g, g, g_diff, g_diff)
        )
    },
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0)) return new Multiply(simple_1, simple_1);
        if (Const.equals(simple_1, 0)) return new Multiply(simple_0, simple_0);
        return new Hypot(simple_0, simple_1);
    },
    function (expression) {
        return expression.constructor === Hypot
            && (expression.expressions === this.expressions || expression.expressions.reverse() === this.expressions);
    },
    "hypot"
);


const HMean = OperationFactory(
    (x, y) => 2 / (1 / x + 1 / y),
    (f, g, f_diff, g_diff) => {
        return new Divide(
            new Multiply(
                Const.TWO,
                new Add(new Multiply(new Multiply(g, g), f_diff), new Multiply(new Multiply(f, f), g_diff))
            ),
            new Multiply(new Add(f, g), new Add(f, g))
        );
    },
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0) || Const.equals(simple_1, 0)) return new Const(0);
        return new HMean(simple_0, simple_1);
    },
    function (expression) {
        return expression.constructor === HMean
            && (expression.expressions === this.expressions || expression.expressions.reverse() === this.expressions);
    },
    "hmean"
);

function parsePrefix(expression) {
    function invalidSymbolError(position, expected, found) {
        return `Invalid symbol on position: ${position}\nExpected: ${expected}\nFound: ${found}`;
    }
    let i = 0;
    function skip(f) {
        for (; i < expression.length && f(expression[i]); i++) {}
    }
    function parseRec() {
        if (i === expression.length) {
            throw invalidSymbolError(i, "bracket expression, variable or constant", "nothing");
        }
        skip((ch) => ch === ' ');
        if (expression[i] === '(') {
            i++;
            skip((ch) => ch === ' ');
            let i0 = i;
            skip((ch) => ch !== ' ' && ch !== '(');
            let token = expression.slice(i0, i);
            if (token in OPERATIONS) {
                let args = [];
                let op = OPERATIONS[token];
                while (args.length !== op.prototype.evaluateImpl.length) {
                    args.push(parseRec());
                }
                skip((ch) => ch === ' ');
                if (i === expression.length || expression[i] !== ')') {
                    throw invalidSymbolError(i, ')', (i < expression.length ? expression[i] : "nothing"));
                }
                i++;
                return new op(...args);
            } else {
                throw invalidSymbolError((i0 + 1) + "-" + i, "operator", expression.slice(i0, i));
            }
        } else {
            let i0 = i;
            skip((ch) => ch !== ' ' && ch !== '(' && ch !== ')');
            let token = expression.slice(i0, i);
            if (token in ARGUMENT_POSITION) {
                return new Variable(token);
            } else {
                let res = new Const(+token);
                if (isNaN(res)) {
                    throw invalidSymbolError((i0 + 1) + "-" + i, "variable or constant", expression.slice(i0, i));
                }
                return res;
            }
        }
    }
    let res = parseRec();
    skip((ch) => ch === ' ');
    if (i < expression.length) {
        throw invalidSymbolError(i, "end of string", expression[i]);
    }
    return res;
}

// println(parse('x y hmean').diff('x').simplify());
// println(parse('5 z /').diff('z').expressions[0].simplify());
// println(parse('5 z /').diff('z').expressions[1].simplify());
// println(parse('5 z /').diff('z').simplify());
// println(parsePrefix("(/ (negate x) 2)"));
// println(parsePrefix.checkCBS("(dflsdjf)dsfjsdj(dsfspdfj)"));
