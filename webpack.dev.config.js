const ScalaJS = require('./scalajs.webpack.config');
const Merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackIncludeAssetsPlugin = require('html-webpack-include-assets-plugin');
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
    new HtmlWebpackPlugin({
      title: 'Scala.js app',
    }),
    new HtmlWebpackIncludeAssetsPlugin({
      assets: [
        { path: 'staticweb-fastopt-loader.js', assetPath: 'staticweb-fastopt-loader.js', type: 'js' },
        { path: 'staticweb-fastopt.js', assetPath: 'staticweb-fastopt.js', type: 'js' }
      ],
      append: true
    })
  ]
});

module.exports = WebApp;
