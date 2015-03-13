/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.hazelcast.listener;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MultiMap;

import org.springframework.integration.hazelcast.context.ApplicationContextStartEventHandler;

/**
 * This is a Hazelcast cluster {@link MembershipListener} in order to listen for
 * membership updates.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastMembershipListener implements MembershipListener {

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		SocketAddress removedMemberSocketAddress = membershipEvent.getMember().getSocketAddress();
		Set<HazelcastInstance> hazelcastLocalInstanceSet = Hazelcast.getAllHazelcastInstances();
		if (!hazelcastLocalInstanceSet.isEmpty()) {
			HazelcastInstance hazelcastInstance = hazelcastLocalInstanceSet.iterator().next();
			Lock lock = hazelcastInstance
					.getLock(ApplicationContextStartEventHandler.HZ_INTERNAL_CONFIGURATION_MULTI_MAP_LOCK);
			lock.lock();
			try {
				MultiMap<SocketAddress, SocketAddress> configMultiMap = hazelcastInstance
						.getMultiMap(ApplicationContextStartEventHandler.HZ_INTERNAL_CONFIGURATION_MULTI_MAP);

				if (configMultiMap.keySet().contains(removedMemberSocketAddress)) {
					SocketAddress newAdminSocketAddress = getNewAdminInstanceSocketAddress(
							configMultiMap, removedMemberSocketAddress);
					for (SocketAddress socketAddress : configMultiMap.values()) {
						if (!socketAddress.equals(removedMemberSocketAddress)) {
							configMultiMap.put(newAdminSocketAddress, socketAddress);
						}
					}
					configMultiMap.remove(removedMemberSocketAddress);
				}
				else {
					configMultiMap.remove(configMultiMap.keySet().iterator().next(), removedMemberSocketAddress);
				}
			}
			finally {
				lock.unlock();
			}
		}
	}

	private SocketAddress getNewAdminInstanceSocketAddress(
			MultiMap<SocketAddress, SocketAddress> configMultiMap, SocketAddress removedMemberSocketAddress) {
		for (SocketAddress socketAddress : configMultiMap.values()) {
			if (!socketAddress.equals(removedMemberSocketAddress)) {
				return socketAddress;
			}
		}

		throw new IllegalStateException("No Active Hazelcast Instance Found.");
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {

	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

	}

}
