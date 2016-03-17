/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.slee.resource.sip11.wrappers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ServerTransactionWrapperAppDataTest {

    private static String WRAPPED_APPLICATION_DATA = "Mary";
    private ServerTransactionWrapperAppData serverTransactionWrapperAppData;

    @Before
    public void testBefore() {
        serverTransactionWrapperAppData = new ServerTransactionWrapperAppData();
    }
    
    @Test
    public void testExternalWhenWrappedApplicationData() throws IOException, ClassNotFoundException {
        // prepare
        serverTransactionWrapperAppData.setWrappedApplicationData(WRAPPED_APPLICATION_DATA);
        // test
        byte[] transactionWrapperAppDataBytes = externalWriteTransactionWrapperAppData();
        ServerTransactionWrapperAppData serverTransactionWrapperAppData = externalReadTransactionWrapperAppData(transactionWrapperAppDataBytes);
        String wrappedApplicationData = (String) serverTransactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(WRAPPED_APPLICATION_DATA, wrappedApplicationData);
    }
    
    @Test
    public void testExternalWhenWrappedApplicationDataIsNull() throws IOException, ClassNotFoundException {
        // prepare
        serverTransactionWrapperAppData.setWrappedApplicationData(null);
        // test
        byte[] transactionWrapperAppDataBytes = externalWriteTransactionWrapperAppData();
        ServerTransactionWrapperAppData serverTransactionWrapperAppData = externalReadTransactionWrapperAppData(transactionWrapperAppDataBytes);
        String wrappedApplicationData = (String) serverTransactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(null, wrappedApplicationData);
    }

    private ServerTransactionWrapperAppData externalReadTransactionWrapperAppData(byte[] transactionWrapperAppDataBytes)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(transactionWrapperAppDataBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (ServerTransactionWrapperAppData) objectInputStream.readObject();
    }

    private byte[] externalWriteTransactionWrapperAppData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(serverTransactionWrapperAppData);
        return byteArrayOutputStream.toByteArray();
    }

}
