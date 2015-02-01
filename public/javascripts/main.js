$(function() {
    var busySpinner = $("#busy-spinner");
    $(document)
        .ajaxStart(function() {
            console.log("spinner show");
            busySpinner.show();
        })
        .ajaxStop(function() {
            console.log("spinner hide");
            $("#busy-spinner").hide();
    });

    $( "#product-id-query" ).autocomplete({
        source: "/items/ids",
        minLength: 2
    });

    $("#close-result").click(function() {
        $(".container .result").slideUp({
            complete: function() {
                $("#product-id-query").val("");
            }
        });
    });

    $("#product-id-query").keypress(function(event) {
        if (event.which == 13) {
            var itemId = $(this).val();
            if (itemId.length !== 0) {
                getItem(itemId);
                event.preventDefault();
            }
        }
    });

    $.validator.addMethod("higherThan", function(value, elem, param) {
        if ($.isNumeric(value)) {
            return value > parseFloat(param.val());
        }
        return false;
    });

    $.validator.addMethod("alphanumericWithSpace", function(value, elem, param) {
        return param && /^[^ ][\w ]*$/.test(value);
    });

    $(".result").validate({
        submitHandler: function() {
            console.log("submit nothing...");
        },
        debug: true,
        rules: {
            title: {
                required: true,
                alphanumericWithSpace: true,
            },
            price: {
                required: true,
                number: true,
                higherThan: $("#cost")
            }
        },
        messages: {
            title: {
                required: "Please enter at least one alphanumeric character.",
                alphanumericWithSpace: "Only letters, numbers, spaces and underscores are allowed."
            },
            price: {
               required: "Price is required.",
               number: "Please use numbers.",
               higherThan: "Price must be higher than cost price."
            }
        }
    });


    $("#title").keypress(function(event) {
        if (event.which == 13 && $(this).valid()) {
            update($("#id").val(), $(this).val(), "/items/title")
            event.preventDefault();        }
    });

    $("#price").keypress(function(event) {
        if (event.which == 13 && $(this).valid()) {
            update($("#id").val(), $(this).val(), "/items/price")
            event.preventDefault();
        }
    });

    function update(id, value, url) {
        $.ajax(url + "/" + id + "/" + value, {
            "type": "POST"
        })
            .fail(function(jqXHR, textStatus, errorThrown) {
                showError("Something bad has happened. Try your luck again later.", 4000);
            })
            .done(function(data, textStatus) {
                showError("Update product ID " + data.id + "'s " + data.item + " with " + data.value + " is successful.");
            });
    }

    function getItem(itemId) {
        $.ajax("/items/" + itemId)
            .fail(function(jqXHR, textStatus, errorThrown) {
                showError("Something bad has happened. Try your luck again later.", 4000);
            })
            .done(function(data, textStatus) {
                var item = data[0];
                if (!item) {
                    showError("<span>Product ID " + itemId + " not found.</span>");
                    return;
                }
                var id = item.id;
                var title = item.title;
                var price = item.pricing.price.toFixed(2);
                var cost = item.pricing.cost.toFixed(2);
                $("#id").val(id);
                $("#title").val(title);
                $("#price").val(price);
                $("#cost").val(cost);
                $(".container .result").slideDown();
            })
    }

    function showError(htmlMessage, duration){
        var infoBox = $("#info-box");
        if (duration == undefined) {
            duration = 2000;
        }
        infoBox.html(htmlMessage);
        infoBox.fadeIn().delay(duration).fadeOut();
    }
});
