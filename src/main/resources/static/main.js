

$(document).ready(function(){
    scrollDown();
    connect();
});

$("#profile-img").click(function () {
    $("#status-options").toggleClass("active");
});

$(".expand-button").click(function () {
    $("#profile").toggleClass("expanded");
    $("#contacts").toggleClass("expanded");
});

$("#status-options ul li").click(function () {
    $("#profile-img").removeClass();
    $("#status-online").removeClass("active");
    $("#status-away").removeClass("active");
    $("#status-busy").removeClass("active");
    $("#status-offline").removeClass("active");
    $(this).addClass("active");

    if ($("#status-online").hasClass("active")) {
        $("#profile-img").addClass("online");
    } else if ($("#status-away").hasClass("active")) {
        $("#profile-img").addClass("away");
    } else if ($("#status-busy").hasClass("active")) {
        $("#profile-img").addClass("busy");
    } else if ($("#status-offline").hasClass("active")) {
        $("#profile-img").addClass("offline");
    } else {
        $("#profile-img").removeClass();
    }
    ;

    $("#status-options").removeClass("active");
});

function newMessage() {
    message = $(".message-input input").val();
    if ($.trim(message) == '') {
        return false;
    }
    if (stompClient) {
        console.log("Send message: " + message);

        let chatMessage = {
            sender: '',
            content: message
        };
        stompClient.send("/app/chat.messages", {}, JSON.stringify(chatMessage));
        $("#chat-content").val('');
    }

    $('<li class="me"><img src="http://emilcarlsson.se/assets/mikeross.png" alt="" /><p>' + message + '</p></li>').appendTo($('.messages ul'));
    $('.message-input input').val(null);
    $('.contact.active .preview').html('<span>You: </span>' + message);
    scrollDown();
};

function scrollDown() {
    $(".messages").animate({scrollTop: $(document).height()}, "fast");
}

$('.submit').click(function () {
    newMessage();
});

$(window).on('keydown', function (e) {
    if (e.which == 13) {
        newMessage();
        return false;
    }
});


function connect() {
    console.log("Connected...")
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    // stompClient.debug = null;

    stompClient.connect({}, function() {
        onSubscribeMessage();
    });
}

function onSubscribeMessage() {
    stompClient.subscribe('/topic/messages', onMessageReceived);
}


function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    $('<li class="bot"><img src="http://emilcarlsson.se/assets/mikeross.png" alt="" /><p>' + message.content + '</p></li>').appendTo($('.messages ul'));
    $('.message-input input').val(null);
    $('.contact.active .preview').html('<span>Bot: </span>' + message.content);

    scrollDown();
}
