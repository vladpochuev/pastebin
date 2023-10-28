const createCanvas = (id) => {
    return new fabric.Canvas(id, {
        width: window.innerWidth,
        height: window.innerHeight,
        selection: false,
        hoverCursor: 'grab',
        moveCursor:'grabbing',
        backgroundColor: '#FFE4DE',
        fireRightClick: true,
        stopContextMenu: true,
    });
}

const initCanvas = () => {
    canvas.renderAll()
    setPanEvents(canvas)
    createGrid(clusterSizeX, clusterSizeY)
    shiftCanvas(clusterSizeX * 3, clusterSizeY)
}

const setPanEvents = (canvas) => {
    canvas.on('mouse:move', e => {
        const point = getExactPoint(e)
        const coords = document.querySelector(".current-coords")
        coords.textContent = `(${point.x};${point.y})`

        if(mousePressed && e.button === 1) {
            canvas.setCursor('grab')
            shiftCanvas(e.e.movementX, e.e.movementY)
        }
    })

    canvas.on('mouse:down', e => {
        if(e.button === 1) {
            mousePressed = true
            canvas.setCursor('grab')
        } else if(e.button === 3 && e.target == null) {
            let point = getExactPoint(e)
            window.lastClickX = point.x
            window.lastClickY = point.y
            openPopup('popup-create-bg')
        }
    })

    canvas.on('mouse:up', e => {
        if(e.button === 1) {
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

const zoom = (delta, offsetX, offsetY) => {
    let zoom = canvas.getZoom()
    zoom *= 0.999 ** delta

    if (zoom > 3) zoom = 3
    if (zoom < 0.05) zoom = 0.05
    const zoomLevel = document.querySelector('#zoom-level')
    zoomLevel.textContent = Math.ceil(zoom * 100) + "%"
    canvas.zoomToPoint({ x: offsetX, y: offsetY }, zoom)
}

const getPointer = (e) => {
    let touch = e.e.touches ? e.e.touches[0] : e.e
    const x = canvas.getPointer(touch).x
    const y = canvas.getPointer(touch).y
    return new fabric.Point(x, y)
}

const getExactPoint = (e) => {
    const pointer = getPointer(e)
    const x = Math.floor(pointer.x/clusterSizeX)
    const y = Math.floor(pointer.y/clusterSizeY)
    return new fabric.Point(x,y)
}

const createObject = (id, title, x, y) => {
    const rect = new fabric.Rect({
        width: clusterSizeX,
        height: clusterSizeY,
        left: clusterSizeX/2 + (x * clusterSizeX),
        top: clusterSizeY/2 + (y * clusterSizeY),
        fill: 'green',
        originX: 'center',
        originY: 'center',
    })

    let text = new fabric.Text(
        `(${x};${y})\n${title}\n${id}`, {
            originX: 'center', originY: 'center',
            left: clusterSizeX/2 + (x * clusterSizeX), top: clusterSizeY/2 + (y * clusterSizeY)
        })

    let group = new fabric.Group([rect, text], {hasControls: false,
        hasBorders: false,
        lockMovementX: true,
        name: id,
        lockMovementY: true})

    group.on('mousemove', () => {
        if(!mousePressed) {
            canvas.setCursor('default')
        }
    })

    group.on('mousedown', e => {
        if(e.button === 3) {
            getAndShowBin(e.target.name)
        }
    })

    canvas.add(group)
}

const createGrid = (x, y) => {
    const properties = {stroke: '#c5897c', evented: false, lockMovementX: true, lockMovementY: true}
    for (let i = -100; i <= 101; i++) {
        canvas.add(new fabric.Line([i * x, 101 * y, i * x, 100 * -y], properties))
    }
    for (let i = -100; i <= 101; i++) {
        canvas.add(new fabric.Line([101 * x, i * y, 100 * -x, i * y], properties))
    }
}

const shiftCanvas = (x, y) => {
    canvas.relativePan(new fabric.Point(x, y))
}