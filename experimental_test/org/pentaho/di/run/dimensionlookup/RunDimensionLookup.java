package org.pentaho.di.run.dimensionlookup;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunDimensionLookup extends TestCase
{
    private static DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);
    
    private static void createIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE DIM_CUSTOMER;");
            System.out.println("Table DIM_CUSTOMER dropped");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }
        System.out.println("Creating table DIM_CUSTOMER ...");
        
        h2db.execStatement(
                "CREATE TABLE DIM_CUSTOMER" + 
                "(" + 
                "    customer_tk IDENTITY" + 
                "    , version INTEGER" + 
                "    , date_from TIMESTAMP" + 
                "    , date_to TIMESTAMP" + 
                "    , id INTEGER" + 
                "    , name VARCHAR(30)" + 
                "    , firstname VARCHAR(30)" + 
                "    , zip INTEGER" + 
                "    , city VARCHAR(30)" + 
                "    , birthdate TIMESTAMP" + 
                "    , street VARCHAR(11)" + 
                "    , housenr INTEGER" + 
                "    , stateCode VARCHAR(9)" + 
                "    , state VARCHAR(30)" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_lookup" + 
                "     ON DIM_CUSTOMER" + 
                "    ( " + 
                "      id" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_tk" + 
                "     ON DIM_CUSTOMER" + 
                "    ( " + 
                "      customer_tk" + 
                "    )" + 
                "    ;"
                );
        System.out.println("Table DIM_CUSTOMER created.");

        h2db.disconnect();
    }

    private static void dropIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE DIM_CUSTOMER;");
            System.out.println("Table DIM_CUSTOMER dropped");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }

        h2db.disconnect();
    }

    private static void truncateDimensionTable() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("TRUNCATE TABLE DIM_CUSTOMER;");
            System.out.println("Table DIM_CUSTOMER truncated");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("Table DIM_CUSTOMER not truncated : "+e.getMessage());
        }

        h2db.disconnect();
    }
    
    public void test__DIMENSION_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("DIMENSION LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DIMENSION_LOOKUP_01_InitialLoad() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupInitialLoad.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        truncateDimensionTable();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    /*
    public void test__DIMENSION_LOOKUP_99() throws KettleDatabaseException
    {
        System.out.println();
        dropIndex();
    }
    */
}
