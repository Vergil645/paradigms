"use strict";

const OPERATIONS = {}
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};
const CONSTANTS = {"one": () => 1, "two": () => 2}

const addOperation = function (operator, arity, func) {
    OPERATIONS[operator] = {arity: arity, func: func};
    return func;
}

const foldLeft = function (init, f, ...args) {
    for (const arg of args) {
        init = f(init, arg);
    }
    return init;
}

const min = (x, y) => x < y ? x : y;
const max = (x, y) => x > y ? x : y;

const cnst = value => () => value;
const variable = (name) => (...args) => args[ARGUMENT_POSITION[name]];
const add = addOperation("+", 2, (f, g) => (...args) => f(...args) + g(...args));
const subtract = addOperation("-", 2, (f, g) => (...args) => f(...args) - g(...args));
const multiply = addOperation("*", 2, (f, g) => (...args) => f(...args) * g(...args));
const divide = addOperation("/", 2, (f, g) => (...args) => f(...args) / g(...args));
const negate = addOperation("negate", 1, f => (...args) => -f(...args));

const one = CONSTANTS["one"];
const two = CONSTANTS["two"];

const min5 = addOperation("min5", 5,
    (...functions) => (...args) => foldLeft(Infinity, (x, func) => min(x, func(...args)), ...functions)
);
const max3 = addOperation("max3", 3,
    (...functions) => (...args) => foldLeft(-Infinity, (x, func) => max(x, func(...args)), ...functions)
);

const parse = expression => {
    let stack = [];
    for (const token of expression.split(" ").filter(word => word !== "")) {
        if (token in OPERATIONS) {
            let op = OPERATIONS[token];
            stack.push(op.func(...stack.splice(-op.arity, op.arity)));
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

const testExpr = add(
    subtract(
        multiply(
            variable('x'),
            variable('x')
        ),
        multiply(
            cnst(2),
            variable('x')
        )
    ),
    cnst(1)
);
for (let x = 0; x <= 10; x++) {
    println(testExpr(x))
}
