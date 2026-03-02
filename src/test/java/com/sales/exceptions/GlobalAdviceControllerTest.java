package com.sales.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sales.request.ErrorDto;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalAdviceControllerTest {

    private final GlobalAdviceController advice = new GlobalAdviceController();

    @Test
    void permissionDeniedHandler() {
        PermissionDeniedDataAccessException ex = new PermissionDeniedDataAccessException("denied", new Exception());
        WebRequest req = mock(WebRequest.class);
        ErrorDto res = advice.permissionDeniedDataAccessException(ex, req);
        assertEquals(403, res.getStatus());
        assertTrue(res.getMessage().contains("denied"));
    }

    @Test
    void notFoundHandler() {
        NotFoundException ex = new NotFoundException("not found");
        WebRequest req = mock(WebRequest.class);
        ErrorDto res = advice.notFoundException(ex, req);
        assertEquals(400, res.getStatus());
    }

    @Test
    void illegalArgAndJsonMapping() {
        IllegalArgumentException iae = new IllegalArgumentException("bad arg");
        WebRequest req = mock(WebRequest.class);
        ErrorDto r1 = advice.illegalArgumentException(iae, req);
        assertEquals(406, r1.getStatus());

        JsonMappingException jme = mock(JsonMappingException.class);
        ErrorDto r2 = advice.jsonMappingExceptionHandle(jme, req);
        assertEquals(406, r2.getStatus());
    }

    @Test
    void multipartAndFileNotFound() {
        org.springframework.web.multipart.MultipartException me = new org.springframework.web.multipart.MultipartException("no multi");
        WebRequest req = mock(WebRequest.class);
        ErrorDto r = advice.noMultipartException(me, req);
        assertEquals(400, r.getStatus());

        FileNotFoundException fe = new FileNotFoundException("missing file");
        ErrorDto rf = advice.fileNotFound(fe, req);
        assertEquals(400, rf.getStatus());
    }

    @Test
    void objectNotFoundAndSqlIntegrity() {
        ObjectNotFoundException one = new ObjectNotFoundException("obj; extra", new Exception());
        WebRequest req = mock(WebRequest.class);
        ErrorDto r = advice.resourceNotFoundException(one, req);
        assertEquals(404, r.getStatus());
        // message may be verbose (ObjectNotFoundException formats a longer message), ensure it contains the expected text
        assertTrue(r.getMessage().contains("No row with the given identifier exists") || r.getMessage().contains("obj"));

        SQLIntegrityConstraintViolationException sql = new SQLIntegrityConstraintViolationException("bad; extra");
        ErrorDto rs = advice.resourceNotFoundException(sql, req);
        assertEquals(500, rs.getStatus());
        assertTrue(rs.getMessage().contains("bad"));
    }

    @Test
    void nullPointerAndDataIntegrityAndConstraint() {
        NullPointerException npe = new NullPointerException("npe");
        WebRequest req = mock(WebRequest.class);
        // This handler attempts to mark the transaction rollback; in tests without a tx this will throw NoTransactionException
        org.springframework.transaction.NoTransactionException thrown = assertThrows(org.springframework.transaction.NoTransactionException.class, () -> advice.resourceNotFoundException(npe, req));
        assertTrue(thrown.getMessage().contains("No transaction"));

        DataIntegrityViolationException dive = new DataIntegrityViolationException("dive", new RuntimeException(new RuntimeException("cause message")));
        ErrorDto rd = advice.dataIntegrityViolationException(dive, req);
        assertEquals(409, rd.getStatus());

        org.hibernate.exception.ConstraintViolationException cve = new org.hibernate.exception.ConstraintViolationException("Duplicate entry 'a' for key 'b'", null, "key");
        ErrorDto rc = advice.sqlExceptionHelper(cve, req);
        assertEquals(409, rc.getStatus());
        assertTrue(rc.getMessage().contains("Duplicate entry"));
    }

    @Test
    void httpAndCustomExceptions() {
        org.springframework.http.converter.HttpMessageNotReadableException hmre = new org.springframework.http.converter.HttpMessageNotReadableException("bad json");
        WebRequest req = mock(WebRequest.class);
        // handler will attempt to set rollback; in a non-tx test this leads to NoTransactionException
        assertThrows(org.springframework.transaction.NoTransactionException.class, () -> advice.httpMessageNotReadableException(hmre, req));

        // MyException will attempt to mark rollback - expect NoTransactionException in unit test
        MyException me = new MyException("myexc");
        assertThrows(org.springframework.transaction.NoTransactionException.class, () -> advice.myException(me, req));

        // UserException also attempts rollback
        UserException ue = new UserException("uexc");
        assertThrows(org.springframework.transaction.NoTransactionException.class, () -> advice.userException(ue, req));

        // UnexpectedRollbackException attempts rollback
        UnexpectedRollbackException ure = new UnexpectedRollbackException("rollback");
        assertThrows(org.springframework.transaction.NoTransactionException.class, () -> advice.unexpectedRollbackException(ure, req));

        // Generic exception handler should return an ErrorDto
        Exception ge = new Exception("outer");
        ErrorDto reg = advice.resourceNotFoundException(ge, req);
        assertEquals(500, reg.getStatus());
    }

}
