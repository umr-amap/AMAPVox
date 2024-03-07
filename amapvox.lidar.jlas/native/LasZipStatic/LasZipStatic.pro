#-------------------------------------------------
#
# Project created by QtCreator 2015-04-07T09:21:23
#
#-------------------------------------------------

QT       -= gui

TARGET = LasZip
TEMPLATE = lib
CONFIG += staticlib

SOURCES += \
    src/arithmeticdecoder.cpp \
    src/arithmeticencoder.cpp \
    src/arithmeticmodel.cpp \
    src/integercompressor.cpp \
    src/lasreaditemcompressed_v1.cpp \
    src/lasreaditemcompressed_v2.cpp \
    src/lasreadpoint.cpp \
    src/lasunzipper.cpp \
    src/laswriteitemcompressed_v1.cpp \
    src/laswriteitemcompressed_v2.cpp \
    src/laswritepoint.cpp \
    src/laszip.cpp \
    src/laszipper.cpp \
    src/arithmeticdecoder.cpp \
    src/arithmeticencoder.cpp \
    src/arithmeticmodel.cpp \
    src/integercompressor.cpp \
    src/lasreaditemcompressed_v1.cpp \
    src/lasreaditemcompressed_v2.cpp \
    src/lasreadpoint.cpp \
    src/lasunzipper.cpp \
    src/laswriteitemcompressed_v1.cpp \
    src/laswriteitemcompressed_v2.cpp \
    src/laswritepoint.cpp \
    src/laszip.cpp \
    src/laszipper.cpp

HEADERS += \
    src/arithmeticdecoder.hpp \
    src/arithmeticencoder.hpp \
    src/arithmeticmodel.hpp \
    src/bytestreamin.hpp \
    src/bytestreamin_file.hpp \
    src/bytestreamin_istream.hpp \
    src/bytestreaminout.hpp \
    src/bytestreaminout_file.hpp \
    src/bytestreamout.hpp \
    src/bytestreamout_file.hpp \
    src/bytestreamout_nil.hpp \
    src/bytestreamout_ostream.hpp \
    src/integercompressor.hpp \
    src/lasreaditem.hpp \
    src/lasreaditemcompressed_v1.hpp \
    src/lasreaditemcompressed_v2.hpp \
    src/lasreaditemraw.hpp \
    src/lasreadpoint.hpp \
    src/lasunzipper.hpp \
    src/laswriteitem.hpp \
    src/laswriteitemcompressed_v1.hpp \
    src/laswriteitemcompressed_v2.hpp \
    src/laswriteitemraw.hpp \
    src/laswritepoint.hpp \
    src/laszip.hpp \
    src/laszip_common_v1.hpp \
    src/laszip_common_v2.hpp \
    src/laszipper.hpp \
    src/mydefs.hpp \
    src/arithmeticdecoder.hpp \
    src/arithmeticencoder.hpp \
    src/arithmeticmodel.hpp \
    src/bytestreamin.hpp \
    src/bytestreamin_file.hpp \
    src/bytestreamin_istream.hpp \
    src/bytestreaminout.hpp \
    src/bytestreaminout_file.hpp \
    src/bytestreamout.hpp \
    src/bytestreamout_file.hpp \
    src/bytestreamout_nil.hpp \
    src/bytestreamout_ostream.hpp \
    src/integercompressor.hpp \
    src/lasreaditem.hpp \
    src/lasreaditemcompressed_v1.hpp \
    src/lasreaditemcompressed_v2.hpp \
    src/lasreaditemraw.hpp \
    src/lasreadpoint.hpp \
    src/lasunzipper.hpp \
    src/laswriteitem.hpp \
    src/laswriteitemcompressed_v1.hpp \
    src/laswriteitemcompressed_v2.hpp \
    src/laswriteitemraw.hpp \
    src/laswritepoint.hpp \
    src/laszip.hpp \
    src/laszip_common_v1.hpp \
    src/laszip_common_v2.hpp \
    src/laszipper.hpp \
    src/mydefs.hpp

unix {
    target.path = /usr/lib
    INSTALLS += target
}

QMAKE_CFLAGS_RELEASE += /MT
QMAKE_CXXFLAGS_RELEASE += /MT

DISTFILES +=

