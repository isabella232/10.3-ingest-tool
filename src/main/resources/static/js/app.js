$(document).ready(function() {
    var toolType = 'transformation';
    var f = $(".submit");
    var i = $(".loader");
    var v = $(".loader_img");
    var u = $("#organization");
    var q = $("#file_type");
    var n = $("#transformation_type");
    var x = $("#mapping_type");
    var g = $("#mapping_location_btn_google");
    var d = "http://localhost:8080/rest/list-organisations";
    var l = "http://localhost:8080/rest/list-mapping-dir-contents";
    var c = "http://localhost:8080/rest/list-output-dir-contents";
    var p = "http://localhost:8080/rest/list-input-dir-contents";
    var e = "http://localhost:8080/rest/list-xquery-dir-contents";
    if (toolType === 'both') {
        console.log('both tool');
        $('.tool_navigation').show();
        $('.tool_navigation a').on('click', function(event) {
            event.preventDefault();
            $('.tool_navigation a').removeClass('active')
            var activeTool = $(this).attr("class");

            $(this).addClass('active')
            console.log(activeTool)
            if (activeTool === 'validation') {
                $('#step1 .source').append('<p style="background-color: #f7bdbd;padding: 6px;text-align: center;">Validation service is currenlty being developed.</p>')
            }
        });
    } else if (toolType === 'transformation'){
        $('.tool_navigation').hide();
        console.log('transformation tool')
    } else if (toolType === 'validation'){
        $('.tool_navigation').hide();
        console.log('validation tool')
    }

    function j() { $(f).prop("disabled", true) }

    function m() { $(f).prop("disabled", false) }

    function b() {
        var y = $("#main-inner").innerHeight();
        var z = (y / 2) - 50;
        $(i).css({ height: y + "px" });
        $(v).css({ "margin-top": z + "px" });
        $(i).show()
    }

    function t() { $(i).hide() }

    function s() {
        $.get(p, function(y) {
            var z = y;
            $.each(z.split("|"), function(A, B) { $("#income_folder").append("<tr><td>" + B + "</td></tr>") })
        })
    }

    function h() {
        $.get(c, function(y) {
            var z = y;
            $.each(z.split("|"), function(A, B) { $("#outcome_folder").append("<tr><td>" + B + "</td></tr>") })
        })
    }

    function k() { $("#income_folder").empty() }

    function r() {
        $("#for_transformation_files").empty();
        $("#transformed_files").empty()
    }

    function o() {
        $(".active").removeClass("active");
        $(".step").slideUp(100);
        j()
    }

    function w() {
        var F = $("#organization").val();
        var A = $("#file_type").val();
        var z = $("#transformation_type").val();
        var D = $("#mapping_type").val();
        var y = $("#google_link").val();
        var C = $("#specific_mapping_google_step").val();
        var E = $("#specific_trans_xquery").val();
        var B;
        if ($("#file_type").val() === "xml_ead") {
            B = "http://localhost:8080/rest/process";
            $.get(B, function(G) {
                var I = G;
                var H = 0;
                $.each(I.split("|"), function(K, L) {
                    var J = L.split("=");
                    var N = J[0];
                    var M = J[1];
                    var result = N.substring(N.lastIndexOf("html") + 5);
                    var address = N + '.html';
                    if (result.indexOf("_DUPLICATE") >= 0) {
                        $("#errors_table").append('<tr style="background-color: #f7bdbd;"><td style="background-color: #f7bdbd;"><span class="getContent" address="' + address + '">' + result + '</span></td><td style="background-color: #f7bdbd;">' + M + '</td></tr>');
                        $(".success_notes").after('<p style="background-color: #f7bdbd;padding: 6px;">' + result + ' containts dublication in EAD wich is not permitted</p>');
                    } else {
                        $("#errors_table").append('<tr><td><span class="getContent" address="' + address + '">' + result + '</span></td><td>' + M + '</td></tr>');
                    }
                    H++
                });
                $('#tableErrors').DataTable();
                $("#transformed_files").append(H);
                $("#step5").slideUp(300);
                $("#step6").slideDown(300);
                $(".active").removeClass("active");
                $("#label_step_6").addClass("active");
                t()
                $('.getContent').on('click', function() {
                    //console.log('clicked');
                    var addressForCall = $(this).attr('address');
                    var settings = { "async": true, "crossDomain": true, "url": 'http://localhost:8080/rest//htmlReport?path=' + addressForCall, "method": "GET", "headers": { "cache-control": "no-cache" } }
                    $.ajax(settings).done(function(response) {
                        var result = fileName.substring(fileName.lastIndexOf("\\") + 1);
                        result = result.replace(".html", "");
                        $('#step6').append('<div class="responseHolder"><h2 style="margin-top: -33px;">' + result + '</h2><span class="closeResponse">X</span>' + response + '</div>');


                        var responseHeight = $('.responseHolder').height();
                        var pageHeight = responseHeight + 200;
                        pageHeight = pageHeight + 'px';
                        $('#page').css('min-height', pageHeight);
                        $(window).scrollTop('0');
                        $('.closeResponse').on('click', function() {
                            $('.responseHolder').remove();
                            $('#page').css('min-height', '500px');
                        })
                    });
                });
            })
        } else {
            if ($("#mapping_type").val() === "generic" && C != "") {
                B = "http://localhost:8080/rest/process?mapping=" + C;
                $.get(B, function(G) {
                    var I = G;
                    var H = 0;
                    $.each(I.split("|"), function(K, L) {
                        var J = L.split("=");
                        var N = J[0];
                        var M = J[1];
                        var result = N.substring(N.lastIndexOf("html") + 5);
                        var address = N + '.html';
                        if (result.indexOf("_DUPLICATE") >= 0) {
                            $("#errors_table").append('<tr style="background-color: #f7bdbd;"><td style="background-color: #f7bdbd;"><span class="getContent" address="' + address + '">' + result + '</span></td><td style="background-color: #f7bdbd;">' + M + '</td></tr>');
                            $(".success_notes").after('<p style="background-color: #f7bdbd;padding: 6px;">' + result + ' containts dublication in EAD wich is not permitted</p>');
                        } else {
                            $("#errors_table").append('<tr><td><span class="getContent" address="' + address + '">' + result + '</span></td><td>' + M + '</td></tr>');
                        }
                        H++
                    });
                    $('#tableErrors').DataTable();
                    $("#transformed_files").append(H);
                    $("#step5").slideUp(300);
                    $("#step6").slideDown(300);
                    $(".active").removeClass("active");
                    $("#label_step_6").addClass("active");
                    t()
                    $('.getContent').on('click', function() {
                        //console.log('clicked');
                        var addressForCall = $(this).attr('address');
                        var settings = { "async": true, "crossDomain": true, "url": 'http://localhost:8080/rest//htmlReport?path=' + addressForCall, "method": "GET", "headers": { "cache-control": "no-cache" } }
                        $.ajax(settings).done(function(response) {
                            var result = fileName.substring(fileName.lastIndexOf("\\") + 1);
                            result = result.replace(".html", "");
                            $('#step6').append('<div class="responseHolder"><h2 style="margin-top: -33px;">' + result + '</h2><span class="closeResponse">X</span>' + response + '</div>');


                            var responseHeight = $('.responseHolder').height();
                            var pageHeight = responseHeight + 200;
                            pageHeight = pageHeight + 'px';
                            $('#page').css('min-height', pageHeight);
                            $(window).scrollTop('0');
                            $('.closeResponse').on('click', function() {
                                $('.responseHolder').remove();
                                $('#page').css('min-height', '500px');
                            })
                        });
                    });
                })
            } else {
                if ($("#mapping_type").val() === "generic" && C === "") {
                    B = "http://localhost:8080/rest/process?organisation=" + F;
                    $("#errors_table").empty();
                    $.get(B, function(G) {
                        var I = G;
                        var H = 0;
                        //console.log(I)
                        $('.duplicate_msg').remove()
                        $.each(I.split("|"), function(K, L) {
                            var J = L.split("=");
                            var N = J[0];
                            var M = J[1];
                            var result = N.substring(N.lastIndexOf("html") + 5);
                            var address = N + '.html';
                            if (result.indexOf("_DUPLICATE") >= 0) {
                                $("#errors_table").append('<tr style="background-color: #f7bdbd;"><td style="background-color: #f7bdbd;"><span class="getContent" address="' + address + '">' + result + '</span></td><td style="background-color: #f7bdbd;">' + M + '</td></tr>');
                                //result = result.replace('_DUPLICATE', '');
                                $(".success_notes").after('<p class="duplicate_msg" style="background-color: #f7bdbd;padding: 6px;">' + result + ' containts dublication in EAD ID wich is not permitted</p>');
                            } else {
                                $("#errors_table").append('<tr><td><span class="getContent" address="' + address + '">' + result + '</span></td><td>' + M + '</td></tr>');
                            }
                            H++
                        });
                        $("#transformed_files").append(H);
                        $("#step5").slideUp(300);
                        $("#step6").slideDown(300);
                        $(".active").removeClass("active");
                        $("#label_step_6").addClass("active");
                        t()
                        $('.getContent').on('click', function() {
                            //alert('clicked')
                            var addressForCall = $(this).attr('address');
                            var fileName = $(this).attr('address');
                            var settings = { "async": true, "crossDomain": true, "url": 'http://localhost:8080/rest//htmlReport?path=' + addressForCall, "method": "GET", "headers": { "cache-control": "no-cache" } }
                            $.ajax(settings).done(function(response) {
                                var result = fileName.substring(fileName.lastIndexOf("\\") + 1);
                                result = result.replace(".html", "");
                                $('#step6').append('<div class="responseHolder"><h2 style="margin-top: -33px;">' + result + '</h2><span class="closeResponse">X</span>' + response + '</div>');


                                var responseHeight = $('.responseHolder').height();
                                var pageHeight = responseHeight + 200;
                                pageHeight = pageHeight + 'px';
                                $('#page').css('min-height', pageHeight);
                                $(window).scrollTop('0');
                                $('.closeResponse').on('click', function() {
                                    $('.responseHolder').remove();
                                    $('#page').css('min-height', '500px');
                                })
                            });
                        });
                        setTimeout(function() {
                            $('#tableErrors').dataTable().fnDestroy();
                            $('#tableErrors').DataTable();
                        }, 1500);
                    })
                } else {
                    if ($("#mapping_type").val() === "specific") {
                        B = "http://localhost:8080/rest/process?xquery=" + E;
                        $.get(B, function(G) {
                            var I = G;
                            var H = 0;
                            $.each(I.split("|"), function(K, L) {
                                var J = L.split("=");
                                var N = J[0];
                                var M = J[1];
                                var result = N.substring(N.lastIndexOf("html") + 5);
                                var address = N + '.html';
                                if (result.indexOf("_DUPLICATE") >= 0) {
                                    $("#errors_table").append('<tr style="background-color: #f7bdbd;"><td style="background-color: #f7bdbd;"><span class="getContent" address="' + address + '">' + result + '</span></td><td style="background-color: #f7bdbd;">' + M + '</td></tr>');
                                    $(".success_notes").after('<p style="background-color: #f7bdbd;padding: 6px;">' + result + ' containts dublication in EAD wich is not permitted</p>');
                                } else {
                                    $("#errors_table").append('<tr><td><span class="getContent" address="' + address + '">' + result + '</span></td><td>' + M + '</td></tr>');
                                }
                                H++
                            });
                            $('#tableErrors').DataTable();
                            $("#transformed_files").append(H);
                            $("#step5").slideUp(300);
                            $("#step6").slideDown(300);
                            $(".active").removeClass("active");
                            $("#label_step_6").addClass("active");
                            t()
                            $('.getContent').on('click', function() {
                                console.log('clicked');
                                var addressForCall = $(this).attr('address');
                                var settings = { "async": true, "crossDomain": true, "url": 'http://localhost:8080/rest//htmlReport?path=' + addressForCall, "method": "GET", "headers": { "cache-control": "no-cache" } }
                                $.ajax(settings).done(function(response) {
                                    var result = fileName.substring(fileName.lastIndexOf("\\") + 1);
                                    result = result.replace(".html", "");
                                    $('#step6').append('<div class="responseHolder"><h2 style="margin-top: -33px;">' + result + '</h2><span class="closeResponse">X</span>' + response + '</div>');
                                    var responseHeight = $('.responseHolder').height();
                                    var pageHeight = responseHeight + 200;
                                    pageHeight = pageHeight + 'px';
                                    $('#page').css('min-height', pageHeight);
                                    $(window).scrollTop('0');
                                    $('.closeResponse').on('click', function() {
                                        $('.responseHolder').remove();
                                        $('#page').css('min-height', '500px');
                                    })
                                });
                            });
                        })
                    }
                }
            }
        }
    }

    function a() {
        if (navigator.onLine) {
            $.get(d, function(z) {
                var y = z;
                $.each(y.split("|"), function(A, B) { $(u).append('<option value="' + B + '">' + B + "</option>") });
                $(u).append('<option value="no_organization">Other</option>')
            })
        } else { $(u).append('<option value="no_organization">Other</option>') }
    }
    $(u).on("change", function() {
        var y = this.value;
        if (y != "") { m() }
    });
    $(q).on("change", function() {
        var y = this.value;
        if (y != "") { m() }
    });
    $(n).on("change", function() {
        var y = this.value;
        if (y != "") { m() }
    });
    $(x).on("change", function() {
        var y = this.value;
        if (y != "") { m() }
    });
    $("#specific_mapping_google_step").on("change", function() {
        var y = $("#specific_mapping_google_step").val();
        if (y != "") {
            $("#view_google").slideUp(300);
            $("#iframe_holder").slideUp(300)
        } else {
            $("#view_google").slideDown(300);
            $("#iframe_holder").slideDown(300)
        }
    });
    a();
    j();
    $(".previous_step").click(function(z) {
        var y = z.target.id;
        if (y === "previous_step2") {
            o();
            $("#label_step_1").addClass("active");
            $("#step1").slideDown(300)
        } else {
            if (y === "previous_step2_1") {
                o();
                $("#label_step_1").addClass("active");
                $("#step1").slideDown(300)
            } else {
                if (y === "previous_step3") {
                    o();
                    $("#label_step_2").addClass("active");
                    $("#step2").slideDown(300)
                } else {
                    if (y === "previous_step4") {
                        o();
                        $("#label_step_3").addClass("active");
                        $("#step3").slideDown(300)
                    } else {
                        if (y === "previous_step4_1") {
                            o();
                            $("#label_step_3").addClass("active");
                            $("#step3").slideDown(300)
                        } else {
                            if (y === "previous_step5") {
                                o();
                                $("#label_step_4").addClass("active");
                                $("#step4").slideDown(300)
                            }
                        }
                    }
                }
            }
        }
    });
    $(".submit_step").click(function(C) {
        var y = C.target.id;
        if (y === "submit_step1") {
            $("#step1").slideUp(300);
            $("#step2").slideDown(300);
            $(".active").removeClass("active");
            $("#label_step_2").addClass("active");
            $("#organization_name").empty();
            var A = $("#organization").val();
            var z = "Organization: " + A;
            $("#organization_name").append(z);
            j()
        } else {
            if (y === "submit_step2") {
                $("#step2").slideUp(300);
                if ($("#file_type").val() === "xml_ead") {
                    $("#step2_1").slideDown(300);
                    j()
                } else {
                    $("#step3").slideDown(300);
                    $(".active").removeClass("active");
                    $("#label_step_3").addClass("active");
                    j()
                }
                j()
            } else {
                if (y === "submit_step2_1") {
                    $("#step2_1").slideUp(300);
                    if ($("#transformation_type").val() === "ead_2") {
                        $("#step5").slideDown(300);
                        $(".active").removeClass("active");
                        $("#label_step_5").addClass("active");
                        s()
                    } else {
                        if ($("#transformation_type").val() === "mapping") {
                            $("#step3").slideDown(300);
                            $(".active").removeClass("active");
                            $("#label_step_3").addClass("active")
                        }
                    }
                    j()
                } else {
                    if (y === "submit_step3") {
                        if ($("#mapping_type").val() === "generic") {
                            if (navigator.onLine) {
                                if ($("#organization").val() != "no_organization") {
                                    var D = "http://localhost:8080/rest/mapping-sheet-ID?organisation=" + $("#organization").val();
                                    $.get(D, function(F) {
                                        var E = "https://docs.google.com/spreadsheets/d/" + F + "/edit?usp=sharing";
                                        $("#view_google").attr("href", E);
                                        $("#google_link").val(E);
                                        $("#google_link").attr("value", E);
                                        $("#google-iframe").attr("src", E);
                                        $("#google-iframe").show();
                                        $("#view_google").show()
                                    })
                                } else {
                                    $("#google-iframe").hide();
                                    $("#view_google").hide()
                                }
                                $("#step3").slideUp(300);
                                $("#step4").slideDown(300)
                            } else {
                                $("#step3").slideUp(300);
                                $("#step4").slideDown(300);
                                $("#view_google").hide();
                                $("#iframe_holder").hide()
                            }
                            $.get(l, function(F) {
                                var E = F;
                                $.each(E.split("|"), function(G, H) { $("#specific_mapping_google_step").append('<option value="' + H + '">' + H + "</option>") })
                            })
                        } else {
                            if ($("#mapping_type").val() === "specific") {
                                $("#step3").slideUp(300);
                                $("#step4_1").slideDown(300);
                                $.get(e, function(E) {
                                    var F = E;
                                    $.each(F.split("|"), function(G, H) { $("#specific_trans_xquery").append('<option value="' + H + '">' + H + "</option>") })
                                })
                            }
                        }
                        $(".active").removeClass("active");
                        $("#label_step_4").addClass("active");
                        C.preventDefault()
                    } else {
                        if (y === "submit_step4") {
                            $("#step4").slideUp(300);
                            $("#step5").slideDown(300);
                            $(".active").removeClass("active");
                            $("#label_step_5").addClass("active");
                            s();
                            j()
                        } else {
                            if (y === "submit_step4_1") {
                                $("#step4_1").slideUp(300);
                                $("#step5").slideDown(300);
                                $(".active").removeClass("active");
                                $("#label_step_5").addClass("active");
                                j();
                                s()
                            } else {
                                if (y === "submit_step5") {
                                    b();
                                    w();
                                    var B = 0;
                                    $.get(p, function(E) {
                                        var F = E;
                                        $.each(F.split("|"), function(G, H) { B++ });
                                        $("#for_transformation_files").append(B)
                                    })
                                } else {
                                    if (y === "start_new") {
                                        $("#step6").slideUp(300);
                                        $("#step2").slideDown(300);
                                        $(".active").removeClass("active");
                                        $("#label_step_2").addClass("active");
                                        $("#file_type").val("");
                                        $("#transformation_type").val("");
                                        $("#mapping_type").val("");
                                        $("#iframe_holder").show();
                                        $("#specific_mapping_google_step").val("");
                                        $("#specific_trans_xquery").val("");
                                        k();
                                        r();
                                        return false
                                    } else {
                                        if (y === "repeat") {
                                            $("#step6").slideUp(300);
                                            $("#step5").slideDown(300);
                                            $(".active").removeClass("active");
                                            $("#label_step_5").addClass("active");
                                            r();
                                            return false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    });
    $(".step_label").click(function(z) {
        var y = z.target.id;
        if (y === "label_step_1") {
            o();
            $("#step1").slideDown(300);
            $(this).addClass("active")
        } else {
            if (y === "label_step_2") {
                if ($("#organization").val() != "") {
                    o();
                    $(this).addClass("active");
                    $("#step2").slideDown(300)
                } else {
                    o();
                    $("#step1").slideDown(300);
                    $("#label_step_1").addClass("active")
                }
            } else {
                if (y === "label_step_3") {
                    if ($("#file_type").val() != "" && $("#organization").val() != "") {
                        o();
                        $(this).addClass("active");
                        $("#step3").slideDown(300)
                    } else {
                        if ($("#organization").val() != "") {
                            o();
                            $("#step2").slideDown(300);
                            $("#label_step_2").addClass("active")
                        } else {
                            o();
                            $("#step1").slideDown(300);
                            $("#label_step_1").addClass("active")
                        }
                    }
                } else {
                    if (y === "label_step_4") {
                        if ($("#file_type").val() != "" && $("#organization").val() != "" && $("#mapping_type").val() != "") {
                            o();
                            $(this).addClass("active");
                            $("#step4").slideDown(300)
                        } else {
                            if ($("#organization").val() != "") {
                                o();
                                $("#step2").slideDown(300);
                                $("#label_step_2").addClass("active")
                            } else {
                                o();
                                $("#step1").slideDown(300);
                                $("#label_step_1").addClass("active")
                            }
                        }
                    } else {
                        if (y === "label_step_5") {
                            if ($("#file_type").val() != "" && $("#organization").val() != "" && $("#mapping_type").val() != "") {
                                o();
                                $(this).addClass("active");
                                $("#step5").slideDown(300)
                            } else {
                                if ($("#organization").val() != "") {
                                    o();
                                    $("#step2").slideDown(300);
                                    $("#label_step_2").addClass("active")
                                } else {
                                    o();
                                    $("#step1").slideDown(300);
                                    $("#label_step_1").addClass("active")
                                }
                            }
                        } else {
                            if (y === "label_step_6") {
                                if ($("#file_type").val() != "" && $("#organization").val() != "" && $("#mapping_type").val() != "") {
                                    o();
                                    $(this).addClass("active");
                                    $("#step6").slideDown(300)
                                } else {
                                    if ($("#organization").val() != "") {
                                        o();
                                        $("#step2").slideDown(300);
                                        $("#label_step_2").addClass("active")
                                    } else {
                                        o();
                                        $("#step1").slideDown(300);
                                        $("#label_step_1").addClass("active")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    })
});
