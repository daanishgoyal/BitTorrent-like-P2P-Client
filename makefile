JC = javac
JFLAGS = -g
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	MessageBitField.java \
	configCommon.java \
  DataPiece.java \
	TransformDataUtil.java \
  DefMessageConfig.java \
	HandShake.java \
	logger.java \
  MessageHandler.java \
  OrderByRate.java \
  PayLoad.java \
  containerPayLoad.java \
  PeerManager.java \
	peerProcess.java \
  PeerStateInfo.java \
RemotePeerInitiator.java \
  ThreadManager.java
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class