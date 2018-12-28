const ScalaJS = require('./scalajs.webpack.config');
const Merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackExcludeAssetsPlugin = require('html-webpack-exclude-assets-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');
const rootDir = path.resolve(__dirname, '../../../..');
const cssDir = path.resolve(rootDir, 'css');

const WebApp = Merge(ScalaJS, {
  mode: 'production',
  entry: {
    styles: [ path.resolve(cssDir, './staticweb.js') ],
    fonts: [ path.resolve(cssDir, './fonts.js') ]
  },
  module: {
    rules: [
      {
        test: /\.p?css$/,
        use: [
          MiniCssExtractPlugin.loader,
          { loader: 'css-loader', options: { importLoaders: 1 } },
          'postcss-loader'
        ]
      }
    ]
  },
  output: {
    filename: "[name].[chunkhash].js",
    path: path.resolve(rootDir, "dist")
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'Scala.js app',
      excludeAssets: [/styles.*.js/, /fonts.*.js/] // excludes redundant js from styles chunk (but includes css)
    }),
    new HtmlWebpackExcludeAssetsPlugin(),
    new MiniCssExtractPlugin({filename: "[name].[contenthash].css"})
  ]
});

module.exports = WebApp;
