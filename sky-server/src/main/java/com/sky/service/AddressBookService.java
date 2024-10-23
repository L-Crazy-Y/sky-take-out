package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

/**
 * 新增收货地址
 */
public interface AddressBookService {
    /**
     * 新增地址
     * @param addressBook
     */
    void addAddress(AddressBook addressBook);

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 根据id修改地址 2024/10/23
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteById(Long id);
}
