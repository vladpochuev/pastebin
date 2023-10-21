const extractData = (form) => {
    form = '#' + form

    let title = $(form + " .title").val()
    let message = $(form + " .message").val()
    let amountOfTime = $(form + " .amount_of_time").val()
    let x, y
    if(form === '#drop-down') {
        x = $("#coords__x").val()
        y = $("#coords__y").val()
    } else if(form === '#pop-up') {
        x = window.lastClickX
        y = window.lastClickY
    }

    const bin = {
        title: title,
        message: message,
        amountOfTime: amountOfTime,
        x: x, y: y}

    ws.sendMessage(bin)
    closePopup()
}

const setFormEvents = () => {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
}

const decodeBins = () => {
    let byteChars = atob(bins)
    let byteArray = new Uint8Array(byteChars.length);
    for (let i = 0; i < byteChars.length; i++) {
        byteArray[i] = byteChars.charCodeAt(i);
    }
    return new TextDecoder().decode(byteArray);
}

const createBinsFromJSON = (json) => {
    let jsonObject = JSON.parse(json);
    jsonObject.forEach(a => createObject(a.id, a.title, a.x, a.y))
}

let mousePressed = false;
let clusterSizeX = Math.floor(window.innerWidth / 7)
let clusterSizeY = Math.floor(window.innerWidth / 7)
let ws = new WS()

const canvas = createCanvas('canvas');
/*const minimap = new fabric.Canvas('minimap', {containerClass: 'minimap', selection: false})*/
initCanvas()
setFormEvents()
createBinsFromJSON(decodeBins())
ws.connect()

const openPopup = (popup) => {
    $('.' + popup).fadeIn(300)
    $('html').addClass('no-scroll')
}

$('.close-popup').click(() => {
    closePopup()
})

$('.popup-create-bg').click(e => {
    if(e.target.className === 'popup-create-bg' && confirm('Are you sure?')) {
        closePopup()
    }
})

$('.popup-show-bg').click(e => {
    if(e.target.className === 'popup-create-bg') {
        closePopup()
    }
})


const closePopup = () => {
    $('.popup-create-bg').fadeOut(200)
    $('.popup-show-bg').fadeOut(200)
    $('html').removeClass('no-scroll')
}

const getAndShowBin = (id) => {
    $.ajax({
        url: '/bin',
        method: 'get',
        dataType: 'json',
        data: {id: id},
        success: function (data) {
            showBin(data)
        }
    });
}

const showBin = (data) => {
    $('.show__title').text(data.title)
    $('.show__options').text(`x:${data.x} y:${data.y}`)
    $('.show__id').text(data.id)
    $('.show__message').text(data.message)

    openPopup('popup-show-bg')
}