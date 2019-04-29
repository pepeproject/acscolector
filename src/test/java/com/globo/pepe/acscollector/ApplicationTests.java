package com.globo.pepe.acscollector;

import com.globo.pepe.acscollector.mock.DataServiceMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    private DataServiceMock dataServiceMock;

	@Test
	public void contextLoads() {
	}

    public DataServiceMock getDataServiceMock() {
        return dataServiceMock;
    }

    public void setDataServiceMock(DataServiceMock dataServiceMock) {
        this.dataServiceMock = dataServiceMock;
    }
}
