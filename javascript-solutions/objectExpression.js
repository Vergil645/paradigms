"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};

function OperationFactory(constructor, ...operators) {
    let res = Object.create(AbstractOperation.prototype);
    res.operator = operators[0];
    res.constructor = constructor;
    for (let operator of operators)
        OPERATIONS[operator] = constructor;
    return res;
}

function parse(expression) {
    const stack = [];
    for (const token of expression.split(" ").filter(word => word !== "")) {
        if (token in OPERATIONS) {
            const op = OPERATIONS[token];
            stack.push(new op(...stack.splice(-op.length)));
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


function Const(value) { this.value = value; }
Const.prototype.constructor = Const;
Const.prototype.evaluate = function () { return this.value; }
Const.prototype.diff = () => new Const(0);
// Const.prototype.simplify = function () { return new Const(this.value); }
Const.prototype.toString = function () { return this.value.toString(); }
Const.equals = (expression, value) => {
    return expression.constructor === Const && (value === undefined || expression.evaluate() === value);
}


function Variable(name) {
    this.arg_pos = ARGUMENT_POSITION[name];
    this.name = name;
}
Variable.prototype.constructor = Variable;
Variable.prototype.evaluate = function (...args) { return args[this.arg_pos]; }
Variable.prototype.diff = function (var_name) { return new Const(this.name === var_name ? 1 : 0); }
// Variable.prototype.simplify = function () { return new Variable(this.name); }
Variable.prototype.toString = function () { return this.name; }


function AbstractOperation(...expressions) { this.expressions = expressions; }
AbstractOperation.prototype.constructor = AbstractOperation;
AbstractOperation.prototype.evaluateImpl = () => undefined;
AbstractOperation.prototype.evaluate = function (...args) {
    return this.evaluateImpl(...this.expressions.map(expr => expr.evaluate(...args)));
}
AbstractOperation.prototype.diffImpl = () => undefined;
AbstractOperation.prototype.diff = function (var_name) {
    return this.diffImpl(...this.expressions.map(expr => expr.diff(var_name)));
}
// AbstractOperation.prototype.simplifyImpl = () => undefined;
// AbstractOperation.prototype.simplify = function () {
//     let simples = this.expressions.map(expr => expr.simplify());
//     for (let simple of simples) {
//         if (!Const.equals(simple))
//             return this.simplifyImpl(...simples);
//     }
//     return new Const(this.evaluateImpl(...simples.map(expr => expr.evaluate())));
// }
AbstractOperation.prototype.operator = undefined;
AbstractOperation.prototype.toString = function () {
    return String.prototype.concat(...this.expressions.map(expr => `${expr.toString()} `), this.operator);
}


function Add(f, g) { AbstractOperation.call(this, f, g); }
Add.prototype = OperationFactory(Add, "+");
Add.prototype.evaluateImpl = (x, y) => x + y;
Add.prototype.diffImpl = (diff_0, diff_1) => new Add(diff_0, diff_1);
// Add.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0)) return simple_1;
//     if (Const.equals(simple_1, 0)) return simple_0;
//     return new Add(simple_0, simple_1);
// }


function Subtract(f, g) { AbstractOperation.call(this, f, g); }
Subtract.prototype = OperationFactory(Subtract, "-");
Subtract.prototype.evaluateImpl = (x, y) => x - y;
Subtract.prototype.diffImpl = (diff_0, diff_1) => new Subtract(diff_0, diff_1);
// Subtract.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0)) return new Negate(simple_1);
//     if (Const.equals(simple_1, 0)) return simple_0;
//     return new Subtract(simple_0, simple_1);
// }


function Multiply(f, g) { AbstractOperation.call(this, f, g); }
Multiply.prototype = OperationFactory(Multiply, "*");
Multiply.prototype.evaluateImpl = (x, y) => x * y;
Multiply.prototype.diffImpl = function (diff_0, diff_1) {
    return new Add(
        new Multiply(this.expressions[0], diff_1),
        new Multiply(diff_0, this.expressions[1])
    );
}
// Multiply.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0) || Const.equals(simple_1, 0)) return new Const(0);
//     if (Const.equals(simple_0, 1)) return simple_1;
//     if (Const.equals(simple_1, 1)) return simple_0;
//     if (Const.equals(simple_0, -1)) return new Negate(simple_1);
//     if (Const.equals(simple_1, -1)) return new Negate(simple_0);
//     return new Multiply(simple_0, simple_1);
// }


function Divide(f, g) { AbstractOperation.call(this, f, g); }
Divide.prototype = OperationFactory(Divide, "/");
Divide.prototype.evaluateImpl = (x, y) => x / y;
Divide.prototype.diffImpl = function (diff_0, diff_1) {
    return new Divide(
        new Subtract(
            new Multiply(diff_0, this.expressions[1]),
            new Multiply(this.expressions[0], diff_1)
        ),
        new Multiply(this.expressions[1], this.expressions[1])
    );
}
// Divide.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0)) return new Const(0);
//     if (Const.equals(simple_1, 1)) return simple_0;
//     if (Const.equals(simple_1, -1)) return new Negate(simple_0);
//     return new Divide(simple_0, simple_1);
// }


function Negate(f) { AbstractOperation.call(this, f); }
Negate.prototype = OperationFactory(Negate, "negate");
Negate.prototype.evaluateImpl = (x) => -x;
Negate.prototype.diffImpl = (diff_0) => new Negate(diff_0);
Negate.prototype.simplifyImpl = (simple_0) => new Negate(simple_0);

function Hypot(f, g) { AbstractOperation.call(this, f, g); }
Hypot.prototype = OperationFactory(Hypot, "hypot");
Hypot.prototype.evaluateImpl = (x, y) => x * x + y * y;
Hypot.prototype.diff = function (var_name) {
    return new Add(
        new Multiply(this.expressions[0], this.expressions[0]),
        new Multiply(this.expressions[1], this.expressions[1])
    ).diff(var_name);
}
// Hypot.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0)) return new Multiply(simple_1, simple_1);
//     if (Const.equals(simple_1, 0)) return new Multiply(simple_0, simple_0);
//     return new Hypot(simple_0, simple_1);
// }

function HMean(f, g) { AbstractOperation.call(this, f, g); }
HMean.prototype = OperationFactory(HMean, "hmean");
HMean.prototype.evaluateImpl = (x, y) => 2 / (1 / x + 1 / y);
HMean.prototype.diff = function (var_name) {
    return new Divide(
        new Const(2),
        new Add(
            new Divide(new Const(1), this.expressions[0]),
            new Divide(new Const(1), this.expressions[1])
        )
    ).diff(var_name);
}
// HMean.prototype.simplifyImpl = (simple_0, simple_1) => {
//     if (Const.equals(simple_0, 0) || Const.equals(simple_1, 0)) return new Const(0);
//     return new HMean(simple_0, simple_1);
// }
