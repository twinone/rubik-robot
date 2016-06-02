var env = process.env["NODE_ENV"];
var webpack = require("webpack");


var assets = "./assets";
var config = {
    entry: "./main",
    output: {
        path: __dirname + "/build",
        filename: "bundle.js",
        publicPath: "/build/"
    },
    module: {
        loaders: [
            { test: /assets\/\.css$/, loader: "style!css" },
        ]
    },
    plugins: [
    ]

};

if (env === "production") {
    config.plugins.push(new webpack.optimize.UglifyJsPlugin());
}

module.exports = config;
