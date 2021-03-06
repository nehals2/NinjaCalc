import CalcVar from './CalcVar'

/**
 * A calculator variable which accepts a generic string.
 */
export class CalcVarInvisible extends CalcVar {
  constructor (initObj) {
    // Make sure equation was provided
    if (!initObj.eqn) {
      throw new Error('Please provide an equation via initObj.eqn() to the CalcVarInvisible.constructor().')
    }
    // Invisible calculator variable cannot be an input
    initObj.typeEqn = () => {
      return 'output'
    }
    super(initObj)
  }

  reCalc = () => {
    // console.log('CalcVarInvisible.reCalc() called for "' + this.name + '".')

    this.val = this.eqn()

    this.validate()
  }

  getVal = () => {
    return this.val
  }

  validate = () => {
    // Do nothing
  }
}
