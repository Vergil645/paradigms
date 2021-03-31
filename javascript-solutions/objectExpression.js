"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};

function ExpressionFactory(Expression, evaluate, diff, simplify, toString) {
    Expression.prototype = {
        constructor: Expression,
        evaluate: evaluate,
        diff: diff,
        simplify: simplify,
        toString: toString
    };
    return Expression;
}

function OperationFactory(evaluateImpl, diffImpl, simplifyImpl, ...operators) {
    function Operation(...expressions) {
        AbstractOperation.call(this, ...expressions);
    }
    Operation.prototype = Object.create(AbstractOperation.prototype);
    Operation.prototype.constructor = Operation;
    Operation.prototype.evaluateImpl = evaluateImpl;
    Operation.prototype.diffImpl = diffImpl;
    Operation.prototype.simplifyImpl = simplifyImpl;
    Operation.prototype.operator = operators[0];
    for (let operator of operators)
        OPERATIONS[operator] = Operation;
    return Operation;
}

function parse(expression) {
    const stack = [];
    for (const token of expression.split(" ").filter(word => word !== "")) {
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
    function () { return this.value.toString(); }
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
    function () { return this.name; }
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
    }
);


const Add = OperationFactory(
    (x, y) => x + y,
    (f, g, f_diff, g_diff) => new Add(f_diff, g_diff),
    (simple_0, simple_1) => {
        if (Const.equals(simple_0, 0)) return simple_1;
        if (Const.equals(simple_1, 0)) return simple_0;
        return new Add(simple_0, simple_1);
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
        return new Divide(simple_0, simple_1);
    },
    "/"
);


const Negate = OperationFactory(
    (x) => -x,
    (f, f_diff) => new Negate(f_diff),
    (simple_0) => new Negate(simple_0),
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
    "hmean"
);
