"use strict";

const OPERATIONS = {};
const CONSTANTS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};

function addOperation(calc, ...operators) {
    const func = (...expressions) => (...args) => calc(...expressions.map(expr => expr(...args)));
    for (const operator of operators) {
        OPERATIONS[operator] = {arity: calc.length, func: func};
    }
    return func;
}
function addConstant(value, ...names) {
    const func = () => value;
    for (const name of names) {
        CONSTANTS[name] = func;
    }
    return func;
}

const cnst = value => () => value;
const variable = (name) => {
    const argIndex = ARGUMENT_POSITION[name];
    return (...args) => args[argIndex];
}
const add = addOperation((x, y) => x + y, "+");
const subtract = addOperation((x, y) => x - y, "-");
const multiply = addOperation((x, y) => x * y, "*");
const divide = addOperation((x, y) => x / y, "/");
const negate = addOperation(x => -x, "negate");

const one = addConstant(1, "one");
const two = addConstant(2, "two");
const madd = addOperation((x, y, z) => x * y + z, "*+", "madd");
const floor = addOperation(Math.floor, "_", "floor");
const ceil = addOperation(Math.ceil, "^", "ceil");

function parse(expression) {
    const stack = [];
    for (const token of expression.split(" ").filter(word => word !== "")) {
        if (token in OPERATIONS) {
            const op = OPERATIONS[token];
            stack.push(op.func(...stack.splice(-op.arity)));
        } else if (token in ARGUMENT_POSITION) {
            stack.push(variable(token));
        } else if (token in CONSTANTS) {
            stack.push(CONSTANTS[token]);
        } else {
            stack.push(cnst(+token));
        }
    }
    return stack.pop();
}
