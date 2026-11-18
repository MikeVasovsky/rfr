package io.student.rangiffler.data.fixture;

import io.student.rangiffler.model.TestUser;

public interface UserFixture extends AutoCloseable{
    TestUser user();
    @Override
    void close();
}
