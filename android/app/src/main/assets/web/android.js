function translateState(state) {

    var s = 3;
    var ss = s*s;

    // ULFRBD -> URFDLB
    var res = state.substring(ss*0, ss*1) + // U
            state.substring(ss*3, ss*4) + // R
            state.substring(ss*2, ss*3) + // F
            state.substring(ss*5, ss*6) + // D
            state.substring(ss*1, ss*2) + // L
            state.substring(ss*4, ss*5); // B
    console.log("State:", res);
    return res;
}

var init = false;

function initSolver() {
  if (!init) {
        init = true;
        Lib.Cube.initSolver();
    }
}


function solve(state) {
    initSolver();

    var c = Lib.Cube.fromString(translateState(state));

    var alg = c.solve();
    c.move(alg);
    console.log("isSolved:", c.isSolved());
    console.log("State:", c.asString());
    var res = [];

    alg.split(" ").forEach(function f(x) {
        if (x[1] == '2') {
            res.push(x[0]); res.push(x[0]);
        }
        else res.push(x);
    });
    console.log(alg);
    console.log(res.length);
    return res.join(" ");
}


// Notify about moves on the cube to Android
cube.moveStartListener = function(move) {
    console.log("notifying android about move start: " + move);
    MoveCallback.onMoveStart(move);
}