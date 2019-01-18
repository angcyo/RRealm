package com.angcyo.realm;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/18
 * Copyright (c) 2019 Shenzhen O&M Cloud Co., Ltd. All rights reserved.
 */
public interface OnRealmAction<T> {
    int onAction(T realm);
}
