'use strict';

function Vote(postId) {
  this.postId = postId;
  this.api = '/api/posts/' + postId + '/vote';
  var self = this;

  this.handleResult = function(success, voteType, result) {
    // console.log(result);
    if (success) {
      $('.post-' + result.id + ' .vote-' + voteType).toggleClass('active');
      $('.post-' + result.id + ' .vote-value').text(result.votes_up - result.votes_down);

      if (voteType == 'up') {
        $('.post-' + result.id + ' .vote-down').removeClass('active');
      } else {
        $('.post-' + result.id + ' .vote-up').removeClass('active');
      }
    } else {
      //
    }
  };

  this.vote = function(voteType) {
    $.ajax({
      url: this.api + '?type=' + voteType,
      method: 'post',
      success: function(res) {
        self.handleResult(true, voteType, res);
      },
      error: function(res) {
        // self.handleResult(false, voteType, res);
      },
      statusCode: {
        401 : function() {
          window.location.href = "/me#/login";
        },
        500 : function() {
          alert("服务器错误");
        }
      }
    });
  };
};

$(function() {
  $.ajaxSetup({
    headers: {
      'X-CSRF-Token': $('meta[name="_csrf"]').attr('content')
    }
  });

  $('.posts-list .post, .post-page').each(function() {
    var id = $(this).attr('data-id');

    $(this).find('.vote-up').click(function() {
      var vote = new Vote(id);
      vote.vote('up');
    });

    $(this).find('.vote-down').click(function() {
      var vote = new Vote(id);
      vote.vote('down');
    });
  });
});
