"use strict";

const CONSTANTS = {};
const OPERATIONS = {};
const ARGUMENT_POSITION = {"x": 0, "y": 1, "z": 2};


function nonCommutativeEquals(expr) {
    let ind = 0;
    return this.expressions().reduce((acc, cur) => acc && cur.equals(expr.expressions()[ind++]), true);
}

function commutativeEquals(expr) {
    let res = false;
    for (let p of commutativeEquals.permutationGenerator(this.evaluateImpl.length)) {
        let ind = 0;
        res ||= this.expressions().reduce((acc, cur) => acc && cur.equals(expr.expressions()[p[ind++]]), true);
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


const AbstractExpressionPrototype = {
    equals: function (expression) {
        return expression.constructor === this.constructor && this.equalsImpl(expression);
    }
}

function ExpressionFactory(Expression, evaluate, diff, simplify, toString, prefix, equalsImpl) {
    Expression.prototype = Object.create(AbstractExpressionPrototype);
    Expression.prototype.constructor = Expression;
    Expression.prototype.evaluate = evaluate;
    Expression.prototype.diff = diff;
    Expression.prototype.simplify = simplify;
    Expression.prototype.toString = toString;
    Expression.prototype.prefix = prefix;
    Expression.prototype.equalsImpl = equalsImpl;
    return Expression;
}

function OperationFactory(evaluateImpl, diffImpl, simplifyImpl, equalsImpl, ...operators) {
    function Operation(...expressions) {
        AbstractOperation.call(this, ...expressions);
    }

    Operation.prototype = Object.create(AbstractOperation.prototype);
    Operation.prototype.constructor = Operation;
    Operation.prototype.evaluateImpl = evaluateImpl;
    Operation.prototype.diffImpl = diffImpl;
    Operation.prototype.simplifyImpl = simplifyImpl;
    Operation.prototype.equalsImpl = equalsImpl;
    Operation.prototype.operator = operators[0];
    for (let operator of operators)
        OPERATIONS[operator] = Operation;
    return Operation;
}


const Const = ExpressionFactory(
    function (value) {
        this.value = () => value;
    },
    function () {
        return this.value();
    },
    () => ZERO,
    function () {
        return new Const(this.value());
    },
    function () {
        return this.value().toString();
    },
    function () {
        return this.value().toString();
    },
    function (expression) {
        return expression.value() === this.value();
    }
);
const ZERO = new Const(0);
const ONE = new Const(1);
const NEG_ONE = new Const(-1);
const TWO = new Const(2);


const Variable = ExpressionFactory(
    function (name) {
        this.arg_pos = () => ARGUMENT_POSITION[name];
        this.name = () => name;
    },
    function (...args) {
        return args[this.arg_pos()];
    },
    function (var_name) {
        return this.name() === var_name ? ONE : ZERO;
    },
    function () {
        return new Variable(this.name());
    },
    function () {
        return this.name();
    },
    function () {
        return this.name();
    },
    function (expression) {
        return expression.name() === this.name();
    }
);


const AbstractOperation = ExpressionFactory(
    function (...expressions) {
        this.expressions = () => expressions;
    },
    function (...args) {
        return this.evaluateImpl(...this.expressions().map(expr => expr.evaluate(...args)));
    },
    function (var_name) {
        return this.diffImpl(...this.expressions(), ...this.expressions().map(expr => expr.diff(var_name)));
    },
    function () {
        let simples = this.expressions().map(expr => expr.simplify());
        for (let simple of simples) {
            if (simple.constructor !== Const) {
                return this.simplifyImpl(...simples);
            }
        }
        return new Const(this.evaluateImpl(...simples.map(expr => expr.value())));
    },
    function () {
        return String.prototype.concat(...this.expressions().map(expr => `${expr.toString()} `), this.operator);
    },
    function () {
        return String.prototype.concat('(', this.operator, ...this.expressions().map(expr => ` ${expr.prefix()}`), ')');
    }
);


const Add = OperationFactory(
    (x, y) => x + y,
    (f, g, df, dg) => new Add(df, dg),
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f)) {
            return g;
        }
        if (Const.prototype.equals.call(ZERO, g)) {
            return f;
        }
        return new Add(f, g);
    },
    commutativeEquals,
    "+"
);


const Subtract = OperationFactory(
    (x, y) => x - y,
    (f, g, df, dg) => new Subtract(df, dg),
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f)) {
            return new Multiply(NEG_ONE, g);
        }
        if (Const.prototype.equals.call(ZERO, g)) {
            return f;
        }
        return new Subtract(f, g);
    },
    nonCommutativeEquals,
    "-"
);


const Multiply = OperationFactory(
    (x, y) => x * y,
    (f, g, df, dg) => {
        return new Add(new Multiply(f, dg), new Multiply(df, g));
    },
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f) || Const.prototype.equals.call(ZERO, g)) {
            return ZERO;
        }
        if (Const.prototype.equals.call(ONE, f)) {
            return g;
        }
        if (Const.prototype.equals.call(ONE, g)) {
            return f;
        }
        return new Multiply(f, g);
    },
    commutativeEquals,
    "*"
);


function createArrayOfMultipliers(expr, array) {
    if (expr.constructor === Multiply) {
        createArrayOfMultipliers(expr.expressions()[0], array);
        createArrayOfMultipliers(expr.expressions()[1], array);
    } else if (expr.constructor === Square) {
        createArrayOfMultipliers(expr.expressions()[0], array);
        createArrayOfMultipliers(expr.expressions()[0], array);
    } else {
        array.push(expr);
    }
    return array;
}

const Divide = OperationFactory(
    (x, y) => x / y,
    (f, g, df, dg) => new Divide(new Subtract(new Multiply(df, g), new Multiply(f, dg)), new Multiply(g, g)),
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f)) {
            return ZERO;
        }
        if (Const.prototype.equals.call(ONE, g)) {
            return f;
        }
        let array0 = createArrayOfMultipliers(f, []);
        let array1 = createArrayOfMultipliers(g, []);
        let flag = true;
        for (let i = 0; i < array0.length; i++) {
            let ind = array1.findIndex((expr) => expr != null && expr.equals(array0[i]));
            if (ind !== -1) {
                array0[i] = null;
                array1[ind] = null;
                flag = false;
            }
        }
        if (flag) {
            return new Divide(f, g);
        }
        f = ONE;
        g = ONE;
        array0.forEach((expr) => {
            if (expr !== null) f = new Multiply(f, expr);
        });
        array1.forEach((expr) => {
            if (expr !== null) g = new Multiply(g, expr);
        });
        return Divide.prototype.simplifyImpl(f.simplify(), g.simplify());
    },
    nonCommutativeEquals,
    "/"
);


const Negate = OperationFactory(
    (x) => -x,
    (f, df) => new Negate(df),
    (f) => new Multiply(NEG_ONE, f),
    nonCommutativeEquals,
    "negate"
);


const Hypot = OperationFactory(
    (x, y) => x * x + y * y,
    (f, g, df, dg) => new Add(Square.prototype.diffImpl(f, df), Square.prototype.diffImpl(g, dg)),
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f)) {
            return new Multiply(g, g);
        }
        if (Const.prototype.equals.call(ZERO, g)) {
            return new Multiply(f, f);
        }
        return new Hypot(f, g);
    },
    commutativeEquals,
    "hypot"
);


const HMean = OperationFactory(
    (x, y) => 2 / (1 / x + 1 / y),
    (f, g, df, dg) => {
        return new Divide(
            new Multiply(TWO, new Add(new Multiply(new Square(g), df), new Multiply(new Square(f), dg))),
            new Square(new Add(f, g))
        );
    },
    (f, g) => {
        if (Const.prototype.equals.call(ZERO, f) || Const.prototype.equals.call(ZERO, g)) {
            return ZERO;
        }
        return new HMean(f, g);
    },
    commutativeEquals,
    "hmean"
);


const Square = OperationFactory(
    (x) => x * x,
    (f, df) => new Multiply(TWO, new Multiply(f, df)),
    (f) => new Square(f),
    nonCommutativeEquals,
    "2 ^"
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
    const gen = parsePrefix.tokenGenerator(expression);

    function parseRec() {
        let token = gen.next();
        if (token.done) {
            throw new parsePrefix.InvalidSymbolError(token, "bracket expression, variable or constant");
        }
        if (token.value.word === '(') {
            token = gen.next();
            if (OPERATIONS.hasOwnProperty(token.value.word)) {
                let args = [];
                let op = OPERATIONS[token.value.word];
                while (args.length !== op.prototype.evaluateImpl.length) {
                    args.push(parseRec());
                }
                token = gen.next();
                if (token.done || token.value.word !== ')') {
                    throw new parsePrefix.InvalidSymbolError(token, ")");
                }
                return new op(...args);
            } else {
                throw new parsePrefix.InvalidSymbolError(token, "operator");
            }
        } else if (ARGUMENT_POSITION.hasOwnProperty(token.value.word)) {
            return new Variable(token.value.word);
        } else {
            let constant = new Const(+token.value.word);
            if (isNaN(constant)) {
                throw new parsePrefix.InvalidSymbolError(token, "variable or constant");
            }
            return constant;
        }
    }

    let res = parseRec();
    let token = gen.next();
    if (!token.done) {
        throw new parsePrefix.InvalidSymbolError(token, "end of string");
    }
    return res;
}

parsePrefix.tokenGenerator = function* (expression) {
    let i = 0;
    while (true) {
        for (; i < expression.length && /^\s$/.test(expression[i]); i++) {
        }
        if (i === expression.length) {
            return {word: '', begin: i, end: i};
        }
        if (expression[i] === '(' || expression[i] === ')') {
            yield {word: expression[i], begin: ++i, end: i};
            continue;
        }
        let i0 = i;
        for (; i < expression.length && !/^\s|\(|\)$/.test(expression[i]); i++) {
        }
        yield {word: expression.slice(i0, i), begin: i0 + 1, end: i};
    }
}

parsePrefix.InvalidSymbolError = function (token, expected) {
    this.message = `Invalid symbol on position: ${token.value.begin}-${token.value.end}\n`;
    this.message.join(`Expected: ${expected}\nFound: '${token.value.word}'`);
}


println(parsePrefix("(/ (* (* x y) (* x x)) (* (* y z) (* x x)))").simplify())

// const T = OperationFactory(
//     (x, y, z) => x + y + z,
//     undefined,
//     undefined,
//     commutativeEquals,
//     ""
// )
// println(new T(new Add(ZERO, ONE), ONE, TWO).equals(new T(TWO, ONE, new Add(ONE, ZERO))))

// println(new Add(Const.ZERO, Const.ZERO).constructor === Add)
// println(parse('x y z * /').diff('x').simplify())
// println(parse('x y z - hypot').diff('z').simplify());
// (/ (* (* 2 y) y) (* (+ x y) (+ x y)))
// (/ 2 (* (+ (/ x y) 1) (+ (/ x y) 1)))
// (* 2 (* (/ y (+ x y)) (/ y (+ x y))))
// println(parse('x y hmean').diff('x').simplify().prefix());
// println(parse('5 z /').diff('z').expressions[0].simplify());
// println(parse('5 z /').diff('z').expressions[1].simplify());
// println(parse('5 z /').diff('z').simplify());
// println(parsePrefix("(/ (negate x) 2)"));
