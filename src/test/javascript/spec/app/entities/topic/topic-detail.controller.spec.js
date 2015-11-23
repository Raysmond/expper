'use strict';

describe('Topic Detail Controller', function() {
    var $scope, $rootScope;
    var MockEntity, MockTopic, MockTag;
    var createController;

    beforeEach(inject(function($injector) {
        $rootScope = $injector.get('$rootScope');
        $scope = $rootScope.$new();
        MockEntity = jasmine.createSpy('MockEntity');
        MockTopic = jasmine.createSpy('MockTopic');
        MockTag = jasmine.createSpy('MockTag');
        

        var locals = {
            '$scope': $scope,
            '$rootScope': $rootScope,
            'entity': MockEntity ,
            'Topic': MockTopic,
            'Tag': MockTag
        };
        createController = function() {
            $injector.get('$controller')("TopicDetailController", locals);
        };
    }));


    describe('Root Scope Listening', function() {
        it('Unregisters root scope listener upon scope destruction', function() {
            var eventType = 'expperApp:topicUpdate';

            createController();
            expect($rootScope.$$listenerCount[eventType]).toEqual(1);

            $scope.$destroy();
            expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
        });
    });
});
