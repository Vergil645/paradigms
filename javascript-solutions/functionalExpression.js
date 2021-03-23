"use strict";

const cnst = value => x => value;
const variable = () => x => x;
const add = (f, g) => x => f(x) + g(x);
const subtract = (f, g) => x => f(x) - g(x);
const multiply = (f, g) => x => f(x) * g(x);
const divide = (f, g) => x => f(x) / g(x);
const negate = f => x => -f(x);

const parse = expression => {
    let i = expression.length - 1;

    const parseConst = () => {
        let value = 0, e = 1;
        while (i >= 0 && '0' <= expression[i] && expression[i] <= '9') {
            value += expression[i] * e;
            e *= 10;
            i--;
        }
        if (i >= 0 && expression[i] === '-') {
            value = -value;
            i--;
        }
        return cnst(value);
    }

    const parseImpl = () => {
        while (i >= 0 && expression[i] === ' ') {
            i--;
        }
        if ('0' <= expression[i] && expression[i] <= '9') {
            return parseConst()
        } else if (expression[i] === 'x') {
            i--;
            return variable();
        } else {
            let operand = expression[i--];
            let g = parseImpl(), f = parseImpl();

            if (operand === '+') return add(f, g);
            else if (operand === '-') return subtract(f, g);
            else if (operand === '*') return multiply(f, g);
            else if (operand === '/') return divide(f, g);
        }
    }
    return parseImpl()
}

//println("Hello!")
