#-------------------------------------------------
#
# Project created by QtCreator 2015-04-09T08:49:16
#
#-------------------------------------------------

QT       -= gui

TARGET = LasZipLibrary
TEMPLATE = lib

DEFINES += LASZIPLIBRARY_LIBRARY

SOURCES += \
    lasziplibrary.cpp

HEADERS += \
    lasziplibrary.h

unix {
    target.path = /usr/lib
    INSTALLS += target
}

win32::QMAKE_CFLAGS_RELEASE += /MT
win32::QMAKE_CXXFLAGS_RELEASE += /MT

unix:!macx: LIBS += -L$$PWD/lib/ -lLasZip

INCLUDEPATH += $$PWD/include
DEPENDPATH += $$PWD/include


win32::INCLUDEPATH += $$PWD/include/win64
win32::DEPENDPATH += $$PWD/include/win64


unix::INCLUDEPATH += $$PWD/include/linux64
unix::DEPENDPATH += $$PWD/include/linux64

unix:!macx: PRE_TARGETDEPS += $$PWD/lib/libLasZip.a

win32:CONFIG(release, debug|release): LIBS += -L$$PWD/lib/ -lLasZip
else:win32:CONFIG(debug, debug|release): LIBS += -L$$PWD/lib/ -lLasZip

win32-g++:CONFIG(release, debug|release): PRE_TARGETDEPS += $$PWD/lib/libLasZip.a
else:win32-g++:CONFIG(debug, debug|release): PRE_TARGETDEPS += $$PWD/lib/libLasZip.a
else:win32:!win32-g++:CONFIG(release, debug|release): PRE_TARGETDEPS += $$PWD/lib/LasZip.lib
else:win32:!win32-g++:CONFIG(debug, debug|release): PRE_TARGETDEPS += $$PWD/lib/LasZip.lib
