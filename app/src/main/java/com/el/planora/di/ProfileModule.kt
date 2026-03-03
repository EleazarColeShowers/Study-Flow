//package com.el.studyflow.di
//
//import com.el.studyflow.data.repository.ProfileRepositoryImpl
//import com.el.studyflow.domain.repository.ProfileRepository
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class ProfileModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
//}