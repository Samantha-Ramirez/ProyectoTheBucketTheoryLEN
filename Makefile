SRCDIR = src
OBJDIR = obj
BINDIR = bin

CFLAGS = -Wall -pthread -I ./include  # Agrega el directorio de las cabeceras
SOURCE = $(wildcard $(SRCDIR)/*.c) # Lista de achivos fuentes
OBJ = $(SOURCE:$(SRCDIR)/%.c=$(OBJDIR)/%.o) # Aplica un map en SOURCE y crea archivos objetos

MD5LIBRARYDIR = resources/md5-lib/libmd5.a
MD5EXECUTEDIR = resources/md5-app/md5
FILE = duplicados
T = 5
D = tests
M = e

all: prepare $(BINDIR)/$(FILE)

clean:
	rm -rf $(OBJDIR) $(BINDIR) || true

prepare:
	xxd -i $(MD5EXECUTEDIR) > include/md5ArrayBinary.h
	mkdir -p $(OBJDIR) $(BINDIR)

$(BINDIR)/$(FILE): $(OBJ)
	gcc $(CFLAGS) -o $@ $^ $(MD5LIBRARYDIR)
	
$(OBJDIR)/%.o: $(SRCDIR)/%.c
	gcc $(CFLAGS) -c $< -o $@

execute: $(BINDIR)/$(FILE)
	./$(BINDIR)/$(FILE) -t $(T) -d $(D) -m $(M)

# CONSTRUCCION Y PRUEBAS DESDE LA CONSOLA
# Compilar: gcc main.c -o main ../resources/md5-lib/libmd5.a
# Ejecutar: ./duplicados -t int -d string -m char