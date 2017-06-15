module.exports = function(grunt) {
	require('load-grunt-tasks')(grunt);
	grunt.initConfig({
		clean : [ "dist" ],
		copy : {
			html_to_dist : {
				cwd : 'src',
				expand : true,
				src : [ '**/*.html' ],
				dest : 'dist'
			},
			dist_to_server : {
				cwd : 'dist',
				expand : true,
				src : [ '**/*.*' ],
				dest : '../server/src/main/resources/webroot'
			}
		}
	});
	grunt.registerTask('build', [ 'clean', 'copy' ]);
	grunt.registerTask('default', [ 'build' ]);
};