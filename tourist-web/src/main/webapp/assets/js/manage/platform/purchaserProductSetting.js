// define(function (require, exports, module) {
$("#modelForm").validate({
    rules: {
        name: {
            required: true,
            maxlength: 100
        },
        explainStr: {
            required: true,
            maxlength: 200
        },
        agreement: {
            required: true
        },
        price: {
            required: true,
            maxlength: 10
        },
        bannerUri: {
            required: true
        }
    },
    messages: {
        name: {
            required: "名称为必输项",
            maxlength: "长度不能超过100个字符"
        },
        explainStr: {
            required: "说明为必输项",
            maxlength: "长度不能超过200个字符"
        },
        agreement: {
            required: "协议必输项",
        },
        price: {
            required: "价格必输项",
            maxlength: "长度不能超过10个字符"
        },
        bannerUri: {
            required: "营业执照为必输项"
        }

    },
    submitHandler: function (form, ev) {
        form.submit();
    },
    invalidHandler: function () {
        return true;
    }
});

// uploader
parent.uploader($('#banner-uploader'), function (path) {
    $("#bannerUri").val(path);
}, {
    allowedExtensions: ['jpeg', 'jpg', 'png', 'bmp'],
    itemLimit: 1,
    sizeLimit: 3 * 1024 * 1024
});
