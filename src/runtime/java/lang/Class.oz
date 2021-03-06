functor

import
   OzmaRuntime('NewObject':NewObject
               'StringLiteral':StringLiteral) at '../../scala/ozma/OzmaRuntime.ozf'
   `functor:java.lang.Object`('type:java.lang.Object':Object
                              'class:java.lang.Object':ObjectClass) at 'Object.ozf'
   `functor:java.lang.Array`(makeNewArrayClass:MakeNewArrayClass
                             newArrayObject:NewArrayObject) at 'Array.ozf'

export
   'type:java.lang.Class':Class
   'class:java.lang.Class':ClassClass

define

   class Class from Object
      attr
         name
         superClass
         mixins
         ancestors
         arrayOfThisClass

      meth '<init>'(ARawName ASuperClass AMixins AAncestors $)
         Object, '<init>#1063877011'(_)
         name := {StringLiteral ARawName}
         superClass := ASuperClass
         mixins := AMixins
         ancestors := AAncestors
         arrayOfThisClass := {ByNeed fun {$}
                                        {MakeNewArrayClass self}
                                     end}
         unit
      end

      meth 'getName#1195259493'($)
         @name
      end

      % For convenience in other Oz functors
      meth getName($)
         @name
      end

      meth 'toString#1195259493'($)
         @name
      end

      meth 'isInstance#-1875011758'(Obj $)
         {self 'isAssignableFrom#1326077541'({Obj getClass($)} $)}
      end

      meth 'isAssignableFrom#1326077541'(SubClass $)
         {SubClass isAncestor(self $)}
      end

      meth isAncestor(Cls $)
         (Cls == self) orelse {Member Cls @ancestors}
      end

      meth 'isArray#-676694176'($)
         false
      end

      meth arrayOfThisClass($)
         @arrayOfThisClass
      end

      meth newArrayOfThisClass(Length $)
         {NewArrayObject @arrayOfThisClass Length}
      end

      meth arrayEncodedName($)
         'L'#{(@name) toRawVS($)}#';'
      end
   end

   ClassClass = {ByNeed fun {$}
                           {NewObject Class ClassClass
                            '<init>'("java.lang.Class"
                                     ObjectClass
                                     nil
                                     [ObjectClass]
                                     _)}
                        end}

end
