SRCDIR = src
BINDIR = bin
MAINCLASS = beerbarrels.BeerBarrels
INPUTFILE = case1.txt

# Lista de archivos fuente
SOURCES = $(wildcard $(SRCDIR)/beerbarrels/*.java)

# Lista de archivos objeto (archivos .class)
CLASSES = $(SOURCES:$(SRCDIR)/%.java=$(BINDIR)/%.class)

all: prepare compile

prepare:
	mkdir -p $(BINDIR)

compile: $(CLASSES)

$(CLASSES): $(SOURCES)
	javac -d $(BINDIR) $(SOURCES)

execute: compile
	java -cp $(BINDIR) $(MAINCLASS) $(INPUTFILE)

# CONSTRUCCION Y PRUEBAS DESDE LA CONSOLA
# make execute
# Para usar un archivo de entrada diferente:
# make execute INPUTFILE=otroArchivo.txt