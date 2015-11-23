'use strict';

describe('Reply Detail Controller', function() {
    var $scope, $rootScope;
    var MockEntity, MockReply, MockUser, MockPost;
    var createController;

    beforeEach(inject(function($injector) {
        $rootScope = $injector.get('$rootScope');
        $scope = $rootScope.$new();
        MockEntity = jasmine.createSpy('MockEntity');
        MockReply = jasmine.createSpy('MockReply');
        MockUser = jasmine.createSpy('MockUser');
        MockPost = jasmine.createSpy('MockPost');
        

        var locals = {
            '$scope': $scope,
            '$rootScope': $rootScope,
            'entity': MockEntity ,
            'Reply': MockReply,
            'User': MockUser,
            'Post': MockPost
        };
        createController = function() {
            $injector.get('$controller')("ReplyDetailController", locals);
        };
    }));


    describe('Root Scope Listening', function() {
        it('Unregisters root scope listener upon scope destruction', function() {
            var eventType = 'expperApp:replyUpdate';

            createController();
            expect($rootScope.$$listenerCount[eventType]).toEqual(1);

            $scope.$destroy();
            expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
        });
    });
});
