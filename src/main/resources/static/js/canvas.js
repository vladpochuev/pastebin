const createCanvas = (id) => {
    return new fabric.Canvas(id, {
        width: window.innerWidth,
        height: window.innerHeight,
        selection: false,
        hoverCursor: 'grab',
        moveCursor: 'grabbing',
        backgroundColor: '#FFE4DE',
        fireRightClick: true,
        stopContextMenu: true,
    });
}

const initCanvas = () => {
    canvas.renderAll()
    setPanEvents(canvas)
    createGrid(clusterSizeX, clusterSizeY)
    getIntoCenter()
}

const setPanEvents = (canvas) => {
    canvas.on('mouse:move', e => {
        const point = getExactPoint(e)
        const coords = document.querySelector(".current_coords")
        coords.textContent = `(${point.x};${-point.y})`

        if (mousePressed && e.button === 1) {
            canvas.setCursor('grab')
            shiftCanvas(e.e.movementX, e.e.movementY)
        }
    })

    canvas.on('mouse:down', e => {
        if (e.button === 1) {
            mousePressed = true
            canvas.setCursor('grab')
        } else if (e.button === 3 && e.target == null) {
            mousePressed = false
            let point = getExactPoint(e)
            window.lastClickX = point.x
            window.lastClickY = point.y
            openPopup('popup-create-bg')
        }
    })

    canvas.on('mouse:up', e => {
        if (e.button === 1) {
            mousePressed = false
            canvas.setCursor('default')
        }
    })

    canvas.on('mouse:wheel', opt => {
        let delta = opt.e.deltaY
        zoom(delta, opt.e.offsetX, opt.e.offsetY)
        opt.e.preventDefault()
        opt.e.stopPropagation()
    })
}

const restrictBorders = (newLeft, newTop, zoom) => {
    if (newLeft < -clusterSizeX * 100) {
        camera.left = -clusterSizeX * 100
    } else if (newLeft + camera.width / zoom > clusterSizeX * 101) {
        camera.left = clusterSizeX * 101 - camera.width / zoom
    } else {
        camera.left = newLeft
    }

    if (newTop < -clusterSizeY * 100) {
        camera.top = -clusterSizeY * 100
    } else if (newTop + camera.height / zoom > clusterSizeY * 101) {
        camera.top = clusterSizeY * 101 - camera.height / zoom
    } else {
        camera.top = newTop
    }
}

const moveInto = (newLeft, newTop, zoom) => {
    restrictBorders(newLeft, newTop, zoom)

    canvas.viewportTransform[4] = -camera.left * zoom;
    canvas.viewportTransform[5] = -camera.top * zoom;

    canvas.relativePan(new fabric.Point(0, 0))
}

const zoom = (delta, offsetX, offsetY) => {
    let zoom = canvas.getZoom();
    zoom *= 0.999 ** (delta * window.devicePixelRatio)

    if (zoom > 3) zoom = 3
    if (zoom < 0.15) zoom = 0.15

    setZoomTitle(zoom)
    canvas.zoomToPoint({x: offsetX, y: offsetY}, zoom)

    const newLeft = -canvas.viewportTransform[4] / zoom;
    const newTop = -canvas.viewportTransform[5] / zoom;
    moveInto(newLeft, newTop, zoom)

    canvas.requestRenderAll()
}

const setZoomTitle = (zoom) => {
    const zoomLevel = document.querySelector('#zoom-level')
    zoomLevel.textContent = Math.ceil(zoom * 100) + "%"
}

const getPointer = (e) => {
    let touch = e.e.touches ? e.e.touches[0] : e.e
    const x = canvas.getPointer(touch).x
    const y = canvas.getPointer(touch).y
    return new fabric.Point(x, y)
}

const getExactPoint = (e) => {
    const pointer = getPointer(e)
    const x = Math.floor(pointer.x / clusterSizeX)
    const y = Math.floor(pointer.y / clusterSizeY)
    return new fabric.Point(x, y)
}

const createObject = (id, title, x, y, color) => {
    const rect = new fabric.Rect({
        width: clusterSizeX,
        height: clusterSizeY,
        left: clusterSizeX / 2 + (x * clusterSizeX),
        top: clusterSizeY / 2 + -(y * clusterSizeY),
        fill: color,
        originX: 'center',
        originY: 'center',
    })

    const validateStringLength = (string) => {
        const maxSize = clusterSizeX
        const ctx = canvas.getContext()
        if (ctx.measureText(string).width * (clusterSizeX / 56) <= maxSize) {
            return string
        } else {
            return trim(string, maxSize, ctx)
        }
    }

    const trim = (string, maxSize, ctx) => {
        let len = ''
        let prevSurrogate = ''
        for (let i = 0; (ctx.measureText(len + '...' + prevSurrogate).width * (maxSize / 56) <= maxSize)
        && i < string.length; i++) {

            let char = string.charAt(i)
            if (char.match(/\p{Surrogate}/gu)) {
                if (prevSurrogate === '') {
                    prevSurrogate = char
                } else {
                    len += prevSurrogate + char
                    prevSurrogate = ''
                }
            } else {
                len += char
            }
        }
        return len + '...'
    }

    const text = validateStringLength(title) + '\n' +
        validateStringLength(`(${x};${y})`) + '\n' +
        validateStringLength(id)

    const defineTextColor = backgroundColor => {
        const rgb = parseInt(backgroundColor.substring(1), 16)
        const r = (rgb >> 16) & 0xff
        const g = (rgb >> 8) & 0xff
        const b = (rgb >> 0) & 0xff

        const brightness = (r * 299 + g * 587 + b * 114) / 1000
        return brightness < 70 ? '#C5C5C5' : '#101010'
    }

    let textBox = new fabric.Textbox(text, {
        originX: 'center', originY: 'center',
        left: clusterSizeX / 2 + (x * clusterSizeX),
        top: clusterSizeY / 2 + -(y * clusterSizeY),
        height: Math.floor(clusterSizeX),
        width: Math.floor(clusterSizeY),
        textAlign: 'center',
        fill: defineTextColor(color),
        fontSize: clusterSizeY / 7
    })

    let group = new fabric.Group([rect, textBox], {
        hasControls: false,
        hasBorders: false,
        lockMovementX: true,
        name: id,
        lockMovementY: true
    })

    group.on('mousemove', () => {
        if (!mousePressed) {
            canvas.setCursor('default')
        }
    })

    group.on('mousedown', e => {
        if (e.button === 3) {
            getAndShowBin(e.target.name)
        }
    })

    field.create(group, x, y)
    canvas.add(group)
}

const createGrid = (x, y) => {
    const properties = {stroke: '#c5897c', evented: false, lockMovementX: true, lockMovementY: true}
    for (let i = -amountOfCellsX; i <= amountOfCellsX + 1; i++) {
        canvas.add(new fabric.Line([i * x, (amountOfCellsY + 1) * y, i * x, amountOfCellsY * -y], properties))
    }
    for (let i = -amountOfCellsY; i <= amountOfCellsY + 1; i++) {
        canvas.add(new fabric.Line([(amountOfCellsX + 1) * x, i * y, amountOfCellsX * -x, i * y], properties))
    }
}

const shiftCanvas = (x, y) => {
    const zoom = canvas.getZoom()
    const newLeft = camera.left - (x / zoom) / window.devicePixelRatio
    const newTop = camera.top - (y / zoom) / window.devicePixelRatio

    moveInto(newLeft, newTop, zoom)

    canvas.requestRenderAll();
}

const getIntoCenter = () => {
    camera.left = -clusterSizeX * 3
    camera.top = -clusterSizeY
    canvas.viewportTransform[4] = -clusterSizeX * 3
    canvas.viewportTransform[5] = -clusterSizeY
    canvas.absolutePan(new fabric.Point(-clusterSizeX * 3, -clusterSizeY))
}

const resizeCanvas = () => {
    canvas.setHeight(window.innerHeight);
    canvas.setWidth(window.innerWidth);
    canvas.renderAll();
}

window.addEventListener('resize', resizeCanvas, false);