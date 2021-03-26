"use strict";

const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};
const CONSTANTS = {"one": () => 1, "two": () => 2};
const elemAdd = (x, y) => x + y;
const elemMul = (x, y) => x * y;

const addOperation = function (arity, func, ...operators) {
    for (const operator of operators) {
        OPERATIONS[operator] = {arity: arity, func: func};
    }
    return func;
}
const addBinaryOperation = (func, ...operators) =>
    addOperation(2, (f, g) => (...args) => func(f(...args), g(...args)), ...operators)
const addTernaryOperation = (func1, func2, ...operators) =>
    addOperation(3, (f, g, p) => (...args) => func1(func2(f(...args), g(...args)), p(...args)), ...operators);

const cnst = value => () => value;
const variable = (name) => (...args) => args[ARGUMENT_POSITION[name]];
const add = addBinaryOperation(elemAdd, "+");
const subtract = addBinaryOperation((x, y) => x - y, "-");
const multiply = addBinaryOperation(elemMul, "*");
const divide = addBinaryOperation((x, y) => x / y, "/");
const negate = addOperation(1, f => (...args) => -f(...args), "negate");

const one = CONSTANTS["one"];
const two = CONSTANTS["two"];
const madd = addTernaryOperation(elemAdd, elemMul, "*+", "madd");
const floor = addOperation(1, f => (...args) => Math.floor(f(...args)), "_", "floor");
const ceil = addOperation(1, f => (...args) => Math.ceil(f(...args)), "^", "ceil");

function parse(expression) {
    let stack = [];
    for (const token of expression.split(" ").filter(word => word !== "")) {
        if (token in OPERATIONS) {
            let op = OPERATIONS[token];
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

// const testExpr = add(
//     subtract(
//         multiply(
//             variable('x'),
//             variable('x')
//         ),
//         multiply(
//             cnst(2),
//             variable('x')
//         )
//     ),
//     cnst(1)
// );
// for (let x = 0; x <= 10; x++) {
//     println(testExpr(x))
// }
