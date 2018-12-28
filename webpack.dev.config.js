const ScalaJS = require('./scalajs.webpack.config');
const Merge = require('webpack-merge');
const path = require('path');
const rootDir = path.resolve(__dirname, '../../../..');
const cssDir = path.resolve(rootDir, 'css');

const WebApp = Merge(ScalaJS, {
  mode: 'development',
  entry: {
    styles: [path.resolve(cssDir, './staticweb.js')]
  },
  module: {
    rules: [
      {
        test: /\.pcss$/,
        use: [
          'style-loader',
          { loader: 'css-loader', options: { importLoaders: 1 } },
          'postcss-loader'
        ]
      }
    ]
  },
  plugins: [

  ]
});

module.exports = WebApp;
