var gulp = require('gulp');
var browserify = require('browserify');
var source = require('vinyl-source-stream');
var tsify = require('tsify');
var sourcemaps = require('gulp-sourcemaps');
var buffer = require('vinyl-buffer');

var watchify = require("watchify");
var gutil = require("gulp-util");

var paths = {
    pages: ['src/*.html']
};

var brfy = browserify({
    basedir: '.',
    debug: true,
    entries: ['src/main.ts'],
    cache: {},
    packageCache: {}
});

var watchedBrowserify = watchify(brfy).plugin(tsify);

gulp.task('copyHtml', function () {
    return gulp.src(paths.pages)
        .pipe(gulp.dest('dist'));
});

function bundle() {
    return watchedBrowserify
        .plugin(tsify)
        .transform('babelify', {
            presets: ['es2015'],
            extensions: ['.ts']
        })
        .bundle()
        .on('error', function (err) {
            console.log(err);
        })
        .pipe(source('bundle.js'))
        .pipe(buffer())
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest('dist'));
}

gulp.task('default', ['copyHtml'], bundle);

watchedBrowserify.on("update", bundle);
watchedBrowserify.on("log", gutil.log);
watchedBrowserify.on("error", gutil.log);
