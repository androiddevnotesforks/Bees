package com.iemkol.beez.di

import com.iemkol.beez.data.repository.FeedsRepositoryImpl
import com.iemkol.beez.data.repository.UserListRepoImpl
import com.iemkol.beez.data.repository.UserRepositoryImpl
import com.iemkol.beez.domain.repository.FeedsRepository
import com.iemkol.beez.domain.repository.UserListRepository
import com.iemkol.beez.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFeedsRepository(
        feedsRepositoryImpl: FeedsRepositoryImpl
    ):FeedsRepository

    @Binds
    @Singleton
    abstract fun bindUserListRepository(
        userListRepoImpl: UserListRepoImpl
    ):UserListRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ):UserRepository
}