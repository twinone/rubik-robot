var dat = require("./rubik-web/assets/vendor/dat.gui.min");

var Cube = require("./rubik-web/assets/cube").Cube;

var solver = require("./rubik-web/assets/solver");
var interpolation = require("./rubik-web/assets/interpolation");

var Face = require("./rubik-web/assets/model").Face;
var State = require("./rubik-web/assets/state").State;
var util = require("./rubik-web/assets/util");
var algorithm = require("./rubik-web/assets/algorithm");


var canvas = document.getElementById("canvas");

var cube = new Cube(canvas, {
  size: Number(util.getQueryParameter("size") || 3),
  showLabels: true,
  state: util.getQueryParameter("state"),

  click: true,
  longClick: false,

  moveEndListener: function(move) {
    controls.state = cube.getState();
    Android.moveEnd(move, controls.state);
  },
});


var gui = new dat.GUI();

var controls = {
  size: cube.size,
  scramble: function(){},
  solve: function(){},
  labels: cube.shouldShowLabels,
  camera: function(){},
  interpolator: function(){},
  duration: cube.anim.duration,
  state: cube.getState(),
  alg: util.getQueryParameter("algorithm") || "",
  button: function(){}, // used for other buttons
};

function initGui() {
  var c = folder("Cube");
  c.add(controls, 'size').min(1).step(1).name("Size")
    .onChange(function(size) { cube.setSize(size); });
  c.add(controls, 'scramble').name("Scramble")
    .onChange(function() { cube.scramble(); });
  c.add(controls, 'solve').name("Solve")
    .onChange(function() { solve(); });

  var v = folder("View");
  v.add(controls, 'labels').name("Show Labels").listen()
    .onFinishChange(function(v) { cube.setLabels(v); });
  v.add(controls, 'camera').name("Reset Camera")
    .onFinishChange(function() { cube.resetCamera(); });

  var a = folder("Animation");
  var interpolators = Object.keys(interpolation.interpolators);
  a.add(controls, "duration").min(0).step(100).setValue(300).name("Duration (ms)")
    .onFinishChange(function(d) { cube.setAnimationDuration(d); });
  a.add(controls, "interpolator", interpolators).setValue(interpolators[0]).name("Interpolator")
    .onFinishChange(function(i) { cube.setInterpolator(i); });

  var st = folder("State");
  st.add(controls, "state").name("Modify State").listen();
  st.add(controls, "button").name("Apply State")
    .onFinishChange(function() { cube.setState(controls.state); });

  var alg = folder("Algorithm");
  alg.add(controls, "alg").name("Edit Algorithm").listen();
  alg.add(controls, "button").name("Run")
    .onFinishChange(function() { cube.algorithm(controls.alg); });
  alg.add(controls, "button").name("Invert")
    .onFinishChange(function() { controls.alg = algorithm.invert(controls.alg); });

  if (window.innerWidth <= 500) gui.close();

  function folder(name) {
    var f = gui.addFolder(name);
    f.open();
    return f;
  }
}
initGui();

canvas.addEventListener("click", clickListener);
canvas.focus();

function clickListener() {
  canvas.focus();
}


function solve() {
  var alg = solver.solve(new State(cube.getState()));
  var opt = algorithm.optimize(alg);
  cube.algorithm(opt);
  console.log("Algorithm:", alg);
}

var cubejsSolver;
var pendingSolves = [];
solver.createCubejsSolver(function (s) {
  cubejsSolver = s;
  pendingSolves.forEach(cubejsSolve);
  delete pendingSolves;
  Android.ready();
});

function cubejsSolve(state) {
  if (!cubejsSolver) return pendingSolves.push();
  cubejsSolver.solve(state, function (alg) {
    Android.solved(state, alg);
  });
}
cube.cubejsSolve = cubejsSolve;

module.exports = cube;
