/**
 * Created by highlandcows on 17/12/14.
 */
module.exports = function (config) {
    config.set({
        basePath: './',
        files: [
            'src/main/webapp/bower_components/angular/angular.js',
            'src/main/webapp/bower_components/angular-material/angular-material.js',
            'src/main/webapp/bower_components/angular-animate/angular-animate.js',
            'src/main/webapp/bower_components/angular-aria/angular-aria.js',
            'src/main/webapp/bower_components/angular-resource/angular-resource.js',
            'src/main/webapp/bower_components/ngstorage/ngStorage.js',
            'src/main/webapp/bower_components/hammerjs/hammer.js',
            'src/main/webapp/bower_components/angular-mocks/angular-mocks.js',
            'src/main/webapp/*.js',
            'src/test/jasmine/*.js'
        ],

        autoWatch: true,

        frameworks: ['jasmine'],
        browser: ['Chrome'],


        plugins: [
            'karma-chrome-launcher',
            'karma-jasmine'
        ]
    })
};
