package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun donation_updatesMonetaryProgressAndMaterialCounts() {
    val repo = com.example.data.repository.SocialConnectRepository()
    val eventBefore = repo.events.value.first { it.id == "evt-2" }
    val initialRaised = eventBefore.donationCampaign.raisedAmount
    val initialDonors = eventBefore.donationCampaign.donorsCount

    // Donate Money
    val moneyMsg = repo.donateMoney("evt-2", 1500.0, "Rahul Nile")
    assertTrue(moneyMsg.contains("Donated ₹1500"))

    val eventAfterMoney = repo.events.value.first { it.id == "evt-2" }
    assertEquals(initialRaised + 1500.0, eventAfterMoney.donationCampaign.raisedAmount, 0.01)
    assertEquals(initialDonors + 1, eventAfterMoney.donationCampaign.donorsCount)
    assertEquals(1500.0, eventAfterMoney.donationCampaign.userMoneyDonated, 0.01)

    // Pledge Material Items (e.g. Toys or Study Material)
    val studyMaterialItem = eventAfterMoney.donationCampaign.materialItems.first { it.category == "Study Material" }
    val initialCollected = studyMaterialItem.collectedQuantity

    val matMsg = repo.donateMaterial("evt-2", studyMaterialItem.id, 5, "Rahul Nile")
    assertTrue(matMsg.contains("Pledged 5"))

    val eventAfterMaterial = repo.events.value.first { it.id == "evt-2" }
    val updatedItem = eventAfterMaterial.donationCampaign.materialItems.first { it.id == studyMaterialItem.id }
    assertEquals(initialCollected + 5, updatedItem.collectedQuantity)
    assertTrue(eventAfterMaterial.donationCampaign.userMaterialDonated.any { it.contains("5 x") })
  }
}
