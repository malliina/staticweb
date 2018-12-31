const ScalaJS = require('./scalajs.webpack.config');
const Merge = require('webpack-merge');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');
const rootDir = path.resolve(__dirname, '../../../..');
const cssDir = path.resolve(rootDir, 'css');

const WebApp = Merge(ScalaJS, {
  mode: 'development',
  entry: {
    styles: [path.resolve(cssDir, './staticweb.js')],
    fonts: [path.resolve(cssDir, './fonts.js')]
  },
  module: {
    rules: [
      {
        test: /\.p?css$/,
        use: [
          MiniCssExtractPlugin.loader,
          {loader: 'css-loader', options: {importLoaders: 1}},
          'postcss-loader'
        ]
      }
    ]
  },
  output: {
    filename: '[name].[chunkhash].js',
  },
  plugins: [
    new MiniCssExtractPlugin({filename: '[name].[contenthash].css'})
  ]
});

module.exports = WebApp;
