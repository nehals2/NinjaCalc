import Vue from 'vue'

const state = {
  // Complete list of calculators that the user can open.
  // These are presented to the user, but filtered first.
  availableCalcs: [],

  // This is updated whenever the search text is changed.
  filteredAvailableCalcs: [],

  openCalcs: []
}

// mutations
const mutations = {
  registerCalc (state, payload) {
    state.availableCalcs.push(payload)
  },
  updateFilteredAvailableCalcs (state, searchText) {
    // Update the filtered available calculators. If the search text is '' (i.e.
    // empty), return all the calculators.
    if (searchText === '') {
      state.filteredAvailableCalcs = state.availableCalcs
      return
    }
    state.filteredAvailableCalcs = state.availableCalcs.filter(calc => {
      // Create regex pattern from search text
      var regex = new RegExp(searchText, 'gi')
      // Search in calculator title (display name)
      if (calc.displayName.match(regex)) return true
      // Search through the tags
      for (var tag of calc.tags) {
        if (tag.match(regex)) return true
      }
    })
  },
  openCalc (state, payload) {
    // Find a unique ID to use
    var maxId = 0
    state.openCalcs.forEach((calc, index) => {
      if (calc.uniqueId > maxId) {
        maxId = calc.uniqueId
      }
    })
    const newUniqueId = maxId + 1

    // Check to make sure componentName is valid
    var foundCalc = state.availableCalcs.find((element) => {
      return element.mainView.name === payload.componentName
    })
    if (!foundCalc) {
      throw new Error('openCalc() requested to open "' + payload.componentName + '", but no calculator with this ID was found in the array of available calculators.')
    }

    state.openCalcs.push({
      name: foundCalc.displayName,
      componentName: foundCalc.mainView.name,
      // Unique ID is used as a unique tab ID
      uniqueId: newUniqueId
    })
  }
}

const actions = {
  /**
   * Call this to register a calculator with the app. This is typically done at
   * start-up.
   * @param context
   * @param value     The calculator you wish to register.
   */
  registerCalc ({state, commit, rootState}, value) {
    Vue.component(value.mainView.name, value.mainView)
    commit('registerCalc', value)
    commit('updateFilteredAvailableCalcs', rootState.searchText)
  },
  openCalc  ({state, commit, rootState}, value) {
    console.log('core.actions.openCalc() called.')
    commit('openCalc', value)
    commit('setLastCalcAsActive')
  }
}

export default {
  state,
  mutations,
  actions
}