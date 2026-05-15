from PIL import Image, ImageDraw

size = 256
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Colors
bg_blue = (52, 152, 219, 230)
paper = (255, 255, 255, 245)
text_dark = (44, 62, 80, 230)
lupa_ring = (80, 80, 80, 200)
lupa_handle = (100, 100, 100, 220)

# Fondo redondeado
draw.rounded_rectangle([(8, 8), (248, 248)], radius=32, fill=bg_blue)

# Hoja de examen blanca
margin = 28
draw.rounded_rectangle([(margin, margin+10), (size-margin, size-20)], radius=12, fill=paper)

# Lineas de texto (preguntas)
line_y = margin + 36
for i in range(5):
    x1 = margin + 16
    x2 = size - margin - 16 - (30 if i % 2 == 0 else 0)
    draw.rectangle([(x1, line_y), (x2, line_y+6)], fill=text_dark)
    line_y += 18

# Checkmarks verdes (revisado)
for i, cy in enumerate([margin+48, margin+84, margin+120]):
    cx = margin + 14
    draw.ellipse([(cx, cy), (cx+10, cy+10)], fill=(39, 174, 96, 220))
    draw.line([(cx+2, cy+5), (cx+4, cy+8), (cx+9, cy+2)], fill=(255, 255, 255, 240), width=3)

# Lupa
lx, ly = size - 75, size - 75
r = 30
draw.ellipse([(lx-r, ly-r), (lx+r, ly+r)], outline=lupa_ring, width=6)
mx, my = lx + r - 8, ly + r - 8
draw.line([(mx, my), (mx+28, my+28)], fill=lupa_handle, width=8)
draw.ellipse([(mx+24, my+24), (mx+32, my+32)], fill=lupa_handle)
draw.arc([(lx-18, ly-18), (lx+2, ly+2)], start=180, end=270, fill=(255, 255, 255, 80), width=4)

img.save('C:/Users/Ziirox-SP/Proyectos/Java/ExamenesCesur/app.ico', format='ICO', sizes=[(256, 256)])
print('ICON OK')
