async function getBin(id) {
    return $.ajax({
        url: '/api/bin',
        method: 'get',
        dataType: 'json',
        data: {id: id},
        success: function (data) {
            return data
        },
        error: function (data) {
            if (data.status === 500) {
                toastr.error('Error while getting the bin')
            } else if (data.status === 404) {
                toastr.error('Bin was not found')
            } else {
                toastr.error('Server is not responding')
            }
        }
    })
}

 function createBin(bin, printMessage = true) {
    const token = $('meta[name="_csrf"]').attr('content');
    const header = $('meta[name="_csrf_header"]').attr('content');
    return $.ajax({
        url: '/api/bin',
        method: 'post',
        dataType: 'json',
        data: bin,
        headers: {
            [header]: token
        },
        success: function (message) {
            showSuccessMessage(message)
            deleteCookie('Bin-to-create')
        },
        error: function (data) {
            if (data.status === 500) {
                toastr.error('Error while deploying the bin')
            } else if (data.status === 409) {
                toastr.error('Bin with selected coordinates already exists')
            } else if (data.status === 406) {
                toastr.error('Bin was created out of bounds')
            } else if (data.status === 400 && !printMessage) {
                console.log('User is not authorized to create new bin')
            } else if (data.status === 400 || data.status === 401) {
                redirectTo('/login')
            } else {
                toastr.error('Server is not responding')
            }
        }
    })
}

const showSuccessMessage = (body) => {
    const linkIconUrl = $('[data-link-icon-url]').attr('data-link-icon-url')
    const linkIconActiveUrl = $('[data-link-icon-active-url]').attr('data-link-icon-active-url')

    toastr.success(`
                <div class='toast__new-bin'>
                    <span>Bin was successfully created</span> 
                    <span class='toast__link'>
                        <img src='${linkIconUrl}' class='link_icon' height='16px' width='16px' alt='copy link'>
                        <img src='${linkIconActiveUrl}' class='link_icon_active' height='16px' width='16px' alt='copy link' onclick='copyUrl("${body.id}")'>
                    </span>
                </div>`, '', {
        onclick: e => onClickSuccessMessage(e, body)
    })
}

const onClickSuccessMessage = (e, body) => {
    if (e.target.className === 'link_icon_active') return
    getIntoCenter()
    canvas.setZoom(1)
    setZoomTitle(1)
    shiftCanvas(-body.x * clusterSizeX, body.y * clusterSizeY)
    openPopup('popup-show')
    getAndShowBin(body.id)
}

function deleteBin(id) {
    const token = $('meta[name="_csrf"]').attr('content');
    const header = $('meta[name="_csrf_header"]').attr('content');
    return $.ajax({
        url: '/api/bin',
        method: 'delete',
        dataType: 'json',
        data: {
            id: id
        },
        headers: {
            [header]: token
        },
        success: function () {
            toastr.success('Bin was successfully deleted')
        },
        error: function (data) {
            if (data.status === 500) {
                toastr.error('Error while deleting the bin')
            } else if (data.status === 404) {
                toastr.error('Bin was not found')
            } else if (data.status === 403) {
                toastr.error('Bin does not belong to you')
            } else if (data.status === 401) {
                redirectTo('/register')
            } else {
                toastr.error('Server is not responding')
            }
        }
    })
}